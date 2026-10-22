package ar.com.platea.service;

import ar.com.platea.domain.Auditorium;
import ar.com.platea.domain.Booking;
import ar.com.platea.domain.SeatReservation;
import ar.com.platea.domain.SeatStatus;
import ar.com.platea.domain.Showtime;
import ar.com.platea.repo.BookingRepository;
import ar.com.platea.repo.SeatReservationRepository;
import ar.com.platea.repo.ShowtimeRepository;
import ar.com.platea.view.CheckoutView;
import ar.com.platea.view.TicketSummary;
import ar.com.platea.view.TicketView;
import ar.com.platea.web.CheckoutForm;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookingService {

	public static final int HOLD_MINUTES = 8;
	private static final Logger log = LoggerFactory.getLogger(BookingService.class);
	private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
	private static final int MAX_SEATS = 8;

	private final ShowtimeRepository showtimes;
	private final SeatReservationRepository seats;
	private final BookingRepository bookings;
	private final HoldCleaner cleaner;
	private final Clock clock;
	private final SecureRandom random = new SecureRandom();

	public BookingService(
			ShowtimeRepository showtimes,
			SeatReservationRepository seats,
			BookingRepository bookings,
			HoldCleaner cleaner,
			Clock clock) {
		this.showtimes = showtimes;
		this.seats = seats;
		this.bookings = bookings;
		this.cleaner = cleaner;
		this.clock = clock;
	}

	@Transactional
	public void hold(long showtimeId, List<String> rawCodes, String sessionId) {
		cleaner.releaseExpired();
		if (rawCodes == null || rawCodes.isEmpty()) {
			throw new BookingException("Elegí al menos una butaca.");
		}
		LinkedHashSet<String> codes = new LinkedHashSet<>();
		for (String raw : rawCodes) {
			if (raw != null && !raw.isBlank()) {
				codes.add(raw.trim().toUpperCase());
			}
		}
		if (codes.isEmpty()) {
			throw new BookingException("Elegí al menos una butaca.");
		}
		if (codes.size() > MAX_SEATS) {
			throw new BookingException("Podés reservar hasta 8 butacas por compra.");
		}

		Showtime show = showtimes.lockById(showtimeId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!show.getStartsAt().isAfter(LocalDateTime.now(clock))) {
			throw new BookingException("Esta función ya comenzó.");
		}
		Auditorium room = show.getAuditorium();
		for (String code : codes) {
			if (!SeatLayout.exists(room, code)) {
				throw new BookingException("Hay una butaca que no existe en esta sala.");
			}
		}

		seats.releaseSession(sessionId, SeatStatus.HELD);
		if (!seats.findByShowtimeIdAndSeatCodeIn(showtimeId, codes).isEmpty()) {
			throw new BookingException("Alguien se quedó con una de esas butacas. Elegí otra.");
		}

		LocalDateTime expires = LocalDateTime.now(clock).plusMinutes(HOLD_MINUTES);
		for (String code : codes) {
			SeatLayout.Position position = SeatLayout.parse(room, code);
			int price = show.getBasePrice() + (position.premium() ? show.getPremiumSurcharge() : 0);
			SeatReservation seat = new SeatReservation();
			seat.setShowtime(show);
			seat.setSeatCode(code);
			seat.setRowLabel(position.rowLabel());
			seat.setSeatNumber(position.number());
			seat.setPremium(position.premium());
			seat.setPrice(price);
			seat.setStatus(SeatStatus.HELD);
			seat.setSessionId(sessionId);
			seat.setExpiresAt(expires);
			seats.save(seat);
		}
	}

	@Transactional
	public void release(String sessionId) {
		seats.releaseSession(sessionId, SeatStatus.HELD);
	}

	@Transactional(readOnly = true)
	public Optional<CheckoutView> preview(String sessionId) {
		cleaner.releaseExpired();
		List<SeatReservation> holds = seats.findHolds(sessionId, SeatStatus.HELD);
		if (holds.isEmpty()) {
			return Optional.empty();
		}
		Showtime show = holds.get(0).getShowtime();
		int total = 0;
		Long expiresAt = null;
		List<CheckoutView.Line> lines = new ArrayList<>();
		for (SeatReservation hold : holds) {
			total += hold.getPrice();
			long epoch = hold.getExpiresAt().atZone(Dates.ZONE).toInstant().toEpochMilli();
			if (expiresAt == null || epoch < expiresAt) {
				expiresAt = epoch;
			}
			String label = "Fila " + hold.getRowLabel() + " · Butaca " + hold.getSeatNumber();
			lines.add(new CheckoutView.Line(hold.getSeatCode(), label, Money.pesos(hold.getPrice()), hold.isPremium()));
		}
		var cinema = show.getAuditorium().getCinema();
		return Optional.of(new CheckoutView(
				show.getId(),
				show.getMovie().getSlug(),
				show.getMovie().getTitle(),
				cinema.getName(),
				cinema.getAddress() + ", " + cinema.getNeighborhood(),
				Dates.when(show.getStartsAt()),
				show.getFormat().label(),
				show.getLanguage().label(),
				lines,
				Money.pesos(total),
				expiresAt,
				Dates.clock(holds.get(0).getExpiresAt())));
	}

	@Transactional
	public Booking confirm(CheckoutForm form, String sessionId) {
		cleaner.releaseExpired();
		List<SeatReservation> holds = seats.findHolds(sessionId, SeatStatus.HELD);
		if (holds.isEmpty()) {
			throw new BookingException("La reserva de tus butacas venció. Volvé a elegirlas.");
		}
		Showtime show = holds.get(0).getShowtime();
		boolean sameShow = holds.stream().allMatch(hold -> hold.getShowtime().getId().equals(show.getId()));
		if (!sameShow) {
			throw new BookingException("Hay un problema con la reserva. Elegí las butacas de nuevo.");
		}
		if (!show.getStartsAt().isAfter(LocalDateTime.now(clock))) {
			throw new BookingException("Esa función ya comenzó.");
		}

		String nombre = form.getNombre().trim().replaceAll("\\s+", " ");
		String email = form.getEmail().trim().toLowerCase();
		String phone = form.getTelefono() == null ? "" : form.getTelefono().trim();
		CardValidator.Card card = CardValidator.check(
				form.getTarjeta(), form.getVencimiento(), form.getCvv(), form.getTitular(), LocalDate.now(clock));

		int total = holds.stream().mapToInt(SeatReservation::getPrice).sum();
		Booking booking = new Booking();
		booking.setCode(newCode());
		booking.setBuyerName(nombre);
		booking.setEmail(email);
		booking.setPhone(phone);
		booking.setCreatedAt(LocalDateTime.now(clock));
		booking.setTotal(total);
		booking.setCardBrand(card.brand());
		booking.setCardLast4(card.last4());
		booking.setShowtime(show);
		bookings.save(booking);

		for (SeatReservation hold : holds) {
			hold.setStatus(SeatStatus.SOLD);
			hold.setSessionId(null);
			hold.setExpiresAt(null);
			hold.setBooking(booking);
			booking.getSeats().add(hold);
		}
		log.info("Entrada emitida {} para {}", booking.getCode(), booking.getEmail());
		return booking;
	}

	@Transactional(readOnly = true)
	public Optional<TicketView> ticket(String code) {
		if (code == null || code.isBlank()) {
			return Optional.empty();
		}
		return bookings.findByCode(code.trim().toUpperCase()).map(this::toTicket);
	}

	@Transactional(readOnly = true)
	public List<TicketSummary> byEmail(String email) {
		return bookings.findByEmailOrderByCreatedAtDesc(email.trim().toLowerCase()).stream().map(this::toSummary).toList();
	}

	private TicketView toTicket(Booking booking) {
		Showtime show = booking.getShowtime();
		var cinema = show.getAuditorium().getCinema();
		List<String> seatLabels = booking.getSeats().stream()
				.map(seat -> seat.getRowLabel() + seat.getSeatNumber())
				.toList();
		return new TicketView(
				booking.getCode(),
				booking.getBuyerName(),
				booking.getEmail(),
				booking.getPhone(),
				show.getMovie().getTitle(),
				cinema.getName(),
				cinema.getAddress(),
				cinema.getCity(),
				show.getAuditorium().getName(),
				Dates.when(show.getStartsAt()),
				show.getFormat().label(),
				show.getLanguage().label(),
				String.join(", ", seatLabels),
				seatLabels.size(),
				Money.pesos(booking.getTotal()),
				booking.getCardBrand() + " ···· " + booking.getCardLast4(),
				Dates.purchased(booking.getCreatedAt()),
				barcode(booking.getCode()));
	}

	private TicketSummary toSummary(Booking booking) {
		Showtime show = booking.getShowtime();
		String seatLabels = booking.getSeats().stream().map(seat -> seat.getRowLabel() + seat.getSeatNumber()).reduce((a, b) -> a + ", " + b).orElse("");
		return new TicketSummary(
				booking.getCode(),
				show.getMovie().getTitle(),
				Dates.when(show.getStartsAt()),
				show.getAuditorium().getCinema().getName(),
				seatLabels,
				Money.pesos(booking.getTotal()));
	}

	private List<TicketView.Bar> barcode(String code) {
		List<TicketView.Bar> bars = new ArrayList<>();
		for (char character : code.toCharArray()) {
			int value = character;
			bars.add(new TicketView.Bar(1 + (value % 3), true));
			bars.add(new TicketView.Bar(value % 2 == 0 ? 2 : 1, false));
		}
		return bars;
	}

	private String newCode() {
		for (int attempt = 0; attempt < 8; attempt++) {
			StringBuilder code = new StringBuilder("PLT-");
			for (int index = 0; index < 6; index++) {
				code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
			}
			if (!bookings.existsByCode(code.toString())) {
				return code.toString();
			}
		}
		throw new IllegalStateException("No se pudo generar el código de entrada");
	}
}
