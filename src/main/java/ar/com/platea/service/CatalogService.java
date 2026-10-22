package ar.com.platea.service;

import ar.com.platea.domain.Auditorium;
import ar.com.platea.domain.Cinema;
import ar.com.platea.domain.Genre;
import ar.com.platea.domain.Movie;
import ar.com.platea.domain.SeatReservation;
import ar.com.platea.domain.SeatStatus;
import ar.com.platea.domain.Showtime;
import ar.com.platea.repo.CinemaRepository;
import ar.com.platea.repo.MovieRepository;
import ar.com.platea.repo.SeatReservationRepository;
import ar.com.platea.repo.ShowtimeRepository;
import ar.com.platea.view.CinemaCard;
import ar.com.platea.view.CinemaGroup;
import ar.com.platea.view.CinemaPage;
import ar.com.platea.view.DayChip;
import ar.com.platea.view.HomeView;
import ar.com.platea.view.MoviePage;
import ar.com.platea.view.ShowView;
import ar.com.platea.view.ShowtimeItem;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogService {

	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
	private static final int HORIZON_DAYS = 6;

	private final MovieRepository movies;
	private final CinemaRepository cinemas;
	private final ShowtimeRepository showtimes;
	private final SeatReservationRepository seats;
	private final HoldCleaner cleaner;
	private final Clock clock;

	public CatalogService(
			MovieRepository movies,
			CinemaRepository cinemas,
			ShowtimeRepository showtimes,
			SeatReservationRepository seats,
			HoldCleaner cleaner,
			Clock clock) {
		this.movies = movies;
		this.cinemas = cinemas;
		this.showtimes = showtimes;
		this.seats = seats;
		this.cleaner = cleaner;
		this.clock = clock;
	}

	public HomeView home(String city, String query, String genreName) {
		cleaner.releaseExpired();
		LocalDateTime now = LocalDateTime.now(clock);
		LocalDate today = now.toLocalDate();
		String q = query == null ? "" : query.trim();
		Genre genre = parseGenre(genreName);
		boolean filtering = !q.isBlank() || genre != null;

		List<Movie> all = movies.findAllByOrderBySortOrderAsc();
		List<Movie> nowShowing = all.stream()
				.filter(Movie::isNowShowing)
				.filter(movie -> genre == null || movie.getGenre() == genre)
				.filter(movie -> q.isBlank() || matches(movie, q))
				.toList();
		List<Movie> upcoming = all.stream()
				.filter(movie -> !movie.isNowShowing())
				.filter(movie -> genre == null || movie.getGenre() == genre)
				.filter(movie -> q.isBlank() || matches(movie, q))
				.toList();

		List<Showtime> upcomingShows = city == null
				? showtimes.findBetween(now, today.plusDays(HORIZON_DAYS).atStartOfDay())
				: showtimes.findBetweenInCity(now, today.plusDays(HORIZON_DAYS).atStartOfDay(), city);
		Map<Long, List<Showtime>> byMovie = new HashMap<>();
		for (Showtime showtime : upcomingShows) {
			byMovie.computeIfAbsent(showtime.getMovie().getId(), id -> new ArrayList<>()).add(showtime);
		}

		Movie featuredMovie = filtering ? null : nowShowing.stream().filter(Movie::isFeatured).findFirst().orElse(null);
		List<Movie> grid = featuredMovie == null
				? nowShowing
				: nowShowing.stream().filter(movie -> !movie.getId().equals(featuredMovie.getId())).toList();

		LocalDateTime endOfToday = today.plusDays(1).atStartOfDay();
		List<Showtime> railShows = List.of();
		String railLabel = "Hoy";
		if (!filtering) {
			railShows = upcomingShows.stream().filter(show -> show.getStartsAt().isBefore(endOfToday)).limit(12).toList();
			if (railShows.isEmpty()) {
				railLabel = "Mañana";
				railShows = upcomingShows.stream()
						.filter(show -> !show.getStartsAt().isBefore(endOfToday) && show.getStartsAt().isBefore(endOfToday.plusDays(1)))
						.limit(12)
						.toList();
			}
		}

		List<HomeView.GenreOption> genres = new ArrayList<>();
		for (Genre value : Genre.values()) {
			genres.add(new HomeView.GenreOption(value.name(), value.label(), value == genre));
		}

		return new HomeView(
				featuredMovie == null ? null : card(featuredMovie, byMovie.get(featuredMovie.getId()), today, city),
				grid.stream().map(movie -> card(movie, byMovie.get(movie.getId()), today, city)).toList(),
				upcoming.stream().map(movie -> upcomingCard(movie)).toList(),
				railShows.stream().map(this::rail).toList(),
				railLabel,
				q,
				genre == null ? "" : genre.name(),
				genres,
				filtering,
				city == null ? "Todas las ciudades" : city,
				nowShowing.size());
	}

	public MoviePage movie(String slug, LocalDate requested, String city) {
		cleaner.releaseExpired();
		Movie movie = movies.findBySlug(slug).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		LocalDateTime now = LocalDateTime.now(clock);
		LocalDate today = now.toLocalDate();
		MoviePage.MovieInfo info = info(movie);

		if (!movie.isNowShowing()) {
			String banner = "El estreno es el " + Dates.release(movie.getReleaseDate()) + ". Todavía no hay funciones a la venta.";
			return new MoviePage(info, List.of(), "", List.of(), banner, false);
		}

		List<Showtime> all = showtimes.findForMovie(movie, now, today.plusDays(HORIZON_DAYS).atStartOfDay());
		if (city != null) {
			all = all.stream().filter(show -> show.getAuditorium().getCinema().getCity().equals(city)).toList();
		}
		if (all.isEmpty()) {
			String banner = city == null
					? "Esta semana no hay funciones."
					: "Esta semana no hay funciones en " + city + ".";
			return new MoviePage(info, List.of(), "", List.of(), banner, false);
		}

		LocalDate earliest = all.stream().map(show -> show.getStartsAt().toLocalDate()).min(Comparator.naturalOrder()).orElse(today);
		LocalDate selected = requested != null && !requested.isBefore(today) && requested.isBefore(today.plusDays(HORIZON_DAYS))
				? requested
				: earliest;

		List<DayChip> days = new ArrayList<>();
		for (int offset = 0; offset < HORIZON_DAYS; offset++) {
			LocalDate date = today.plusDays(offset);
			boolean available = all.stream().anyMatch(show -> show.getStartsAt().toLocalDate().equals(date));
			days.add(new DayChip(date.toString(), Dates.chip(date, today), date.equals(selected), available));
		}

		List<Showtime> dayShows = all.stream().filter(show -> show.getStartsAt().toLocalDate().equals(selected)).toList();
		Map<Long, Long> occupied = occupied(dayShows.stream().map(Showtime::getId).toList());
		Map<Long, CinemaGroup> groups = new LinkedHashMap<>();
		for (Showtime show : dayShows) {
			Cinema cinema = show.getAuditorium().getCinema();
			groups.computeIfAbsent(cinema.getId(), id -> new CinemaGroup(
					cinema.getName(),
					cinema.getSlug(),
					cinema.getNeighborhood() + " · " + cinema.getAddress(),
					new ArrayList<>()));
			((ArrayList<ShowtimeItem>) groups.get(cinema.getId()).showtimes()).add(item(show, occupied));
		}
		String banner = groups.isEmpty()
				? "No hay funciones ese día" + (city == null ? "." : " en " + city + ".")
				: null;
		return new MoviePage(info, days, Dates.longDate(selected), List.copyOf(groups.values()), banner, true);
	}

	public ShowView showtime(long id, String sessionId) {
		cleaner.releaseExpired();
		Showtime show = showtimes.findDetailed(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Auditorium room = show.getAuditorium();
		Cinema cinema = room.getCinema();
		Map<String, SeatReservation> taken = new HashMap<>();
		for (SeatReservation reservation : seats.findByShowtimeId(id)) {
			taken.put(reservation.getSeatCode(), reservation);
		}

		int selectedCount = 0;
		int selectedTotal = 0;
		Long expiresAt = null;
		List<ShowView.SeatRow> rows = new ArrayList<>();
		List<List<Integer>> blocks = SeatLayout.blocks(room.getSeatsPerRow());
		for (int rowIndex = 0; rowIndex < room.getRowCount(); rowIndex++) {
			String letter = SeatLayout.rowLabel(rowIndex);
			List<List<ShowView.SeatCell>> rowBlocks = new ArrayList<>();
			for (List<Integer> block : blocks) {
				List<ShowView.SeatCell> cells = new ArrayList<>();
				for (int number : block) {
					boolean premium = SeatLayout.premium(rowIndex, number, room.getRowCount(), room.getSeatsPerRow());
					int price = show.getBasePrice() + (premium ? show.getPremiumSurcharge() : 0);
					String code = letter + number;
					SeatReservation reservation = taken.get(code);
					String state = "libre";
					if (reservation != null) {
						boolean mine = reservation.getStatus() == SeatStatus.HELD && sessionId.equals(reservation.getSessionId());
						state = mine ? "mia" : "ocupada";
						if (mine) {
							selectedCount++;
							selectedTotal += price;
							if (expiresAt == null || reservation.getExpiresAt().atZone(Dates.ZONE).toInstant().toEpochMilli() < expiresAt) {
								expiresAt = reservation.getExpiresAt().atZone(Dates.ZONE).toInstant().toEpochMilli();
							}
						}
					}
					cells.add(new ShowView.SeatCell(code, number, premium, price, Money.pesos(price), state));
				}
				rowBlocks.add(List.copyOf(cells));
			}
			rows.add(new ShowView.SeatRow(letter, rowBlocks));
		}

		boolean closed = !show.getStartsAt().isAfter(LocalDateTime.now(clock));
		return new ShowView(
				show.getId(),
				show.getMovie().getSlug(),
				show.getMovie().getTitle(),
				cinema.getName(),
				cinema.getSlug(),
				cinema.getNeighborhood(),
				cinema.getAddress(),
				cinema.getCity(),
				room.getName(),
				Dates.when(show.getStartsAt()),
				show.getStartsAt().toLocalDate().toString(),
				show.getFormat().label(),
				show.getFormat().key(),
				show.getLanguage().label(),
				show.getMovie().getRating().label(),
				show.getMovie().getRating().key(),
				rows,
				selectedCount,
				Money.pesos(selectedTotal),
				closed,
				expiresAt,
				BookingService.HOLD_MINUTES);
	}

	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public List<CinemaCard> cinemas(String city) {
		return cinemas.findAllByOrderBySortOrderAsc().stream()
				.filter(cinema -> city == null || cinema.getCity().equals(city))
				.map(cinema -> new CinemaCard(
						cinema.getSlug(),
						cinema.getName(),
						cinema.getCity(),
						cinema.getNeighborhood(),
						cinema.getAddress(),
						cinema.getAuditoriums().size()))
				.toList();
	}

	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public CinemaPage cinema(String slug) {
		cleaner.releaseExpired();
		Cinema cinema = cinemas.findBySlug(slug).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		LocalDateTime now = LocalDateTime.now(clock);
		LocalDate today = now.toLocalDate();
		LocalDateTime end = today.plusDays(2).atStartOfDay();
		List<Showtime> shows = showtimes.findForCinema(cinema.getId(), now, end);
		Map<Long, Long> occupied = occupied(shows.stream().map(Showtime::getId).toList());
		Map<Long, CinemaPage.CinemaMovie> grouped = new LinkedHashMap<>();
		for (Showtime show : shows) {
			Movie movie = show.getMovie();
			grouped.computeIfAbsent(movie.getId(), id -> new CinemaPage.CinemaMovie(
					movie.getTitle(),
					movie.getSlug(),
					movie.getRating().label(),
					movie.getRating().key(),
					movie.getMotif(),
					movie.getPosterBg(),
					movie.getPosterAccent(),
					new ArrayList<>()));
			((ArrayList<ShowtimeItem>) grouped.get(movie.getId()).showtimes()).add(item(show, occupied));
		}
		String window = shows.isEmpty()
				? "Hoy y mañana"
				: shows.stream().anyMatch(show -> show.getStartsAt().toLocalDate().equals(today)) ? "Hoy y mañana" : "Mañana";
		return new CinemaPage(
				cinema.getName(),
				cinema.getCity(),
				cinema.getNeighborhood(),
				cinema.getAddress(),
				cinema.getSlug(),
				cinema.getAuditoriums().size(),
				window,
				List.copyOf(grouped.values()));
	}

	private HomeView.MovieCard card(Movie movie, List<Showtime> shows, LocalDate today, String city) {
		Showtime next = shows == null || shows.isEmpty() ? null : shows.get(0);
		String nextShow = next == null
				? (city == null ? "Sin funciones esta semana" : "Sin funciones en esta ciudad")
				: Dates.relativeTime(next.getStartsAt(), today) + " · " + next.getAuditorium().getCinema().getNeighborhood();
		String priceFrom = next == null ? "" : "Desde " + Money.pesos(shows.stream().mapToInt(Showtime::getBasePrice).min().orElse(next.getBasePrice()));
		return new HomeView.MovieCard(
				movie.getSlug(),
				movie.getTitle(),
				movie.getTagline(),
				movie.getGenre().label(),
				movie.getRating().label(),
				movie.getRating().key(),
				movie.getRating().hint(),
				movie.getRuntimeMinutes() + " min",
				movie.getDirector(),
				movie.getPosterBg(),
				movie.getPosterAccent(),
				movie.getMotif(),
				nextShow,
				priceFrom,
				next != null);
	}

	private HomeView.MovieCard upcomingCard(Movie movie) {
		return new HomeView.MovieCard(
				movie.getSlug(),
				movie.getTitle(),
				movie.getTagline(),
				movie.getGenre().label(),
				movie.getRating().label(),
				movie.getRating().key(),
				movie.getRating().hint(),
				movie.getRuntimeMinutes() + " min",
				movie.getDirector(),
				movie.getPosterBg(),
				movie.getPosterAccent(),
				movie.getMotif(),
				"Estrena el " + Dates.release(movie.getReleaseDate()),
				"",
				false);
	}

	private HomeView.RailItem rail(Showtime show) {
		return new HomeView.RailItem(
				show.getId(),
				show.getStartsAt().format(TIME),
				show.getMovie().getTitle(),
				show.getAuditorium().getCinema().getNeighborhood(),
				show.getFormat().label(),
				show.getFormat().key(),
				show.getMovie().getSlug());
	}

	private MoviePage.MovieInfo info(Movie movie) {
		return new MoviePage.MovieInfo(
				movie.getSlug(),
				movie.getTitle(),
				movie.getTagline(),
				movie.getSynopsis(),
				movie.getGenre().label(),
				movie.getRating().label(),
				movie.getRating().key(),
				movie.getRating().hint(),
				movie.getRuntimeMinutes() + " min",
				movie.getDirector(),
				movie.getCastNames(),
				Dates.release(movie.getReleaseDate()),
				movie.getPosterBg(),
				movie.getPosterAccent(),
				movie.getMotif(),
				movie.isNowShowing());
	}

	private ShowtimeItem item(Showtime show, Map<Long, Long> occupied) {
		long taken = occupied.getOrDefault(show.getId(), 0L);
		boolean soldOut = taken >= show.getAuditorium().capacity();
		return new ShowtimeItem(
				show.getId(),
				show.getStartsAt().format(TIME),
				show.getFormat().label(),
				show.getFormat().key(),
				show.getLanguage().chip(),
				show.getAuditorium().getName(),
				Money.pesos(show.getBasePrice()),
				soldOut);
	}

	private Map<Long, Long> occupied(List<Long> ids) {
		if (ids.isEmpty()) {
			return Map.of();
		}
		Map<Long, Long> counts = new HashMap<>();
		for (Object[] row : seats.countByShowtimeIds(ids)) {
			counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
		}
		return counts;
	}

	private boolean matches(Movie movie, String query) {
		String needle = Slugs.fold(query);
		return Slugs.fold(movie.getTitle()).contains(needle)
				|| Slugs.fold(movie.getDirector()).contains(needle)
				|| Slugs.fold(movie.getGenre().label()).contains(needle)
				|| Slugs.fold(movie.getCastNames()).contains(needle);
	}

	private Genre parseGenre(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return Genre.valueOf(raw.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}
}
