package ar.com.platea.service;

import ar.com.platea.domain.Auditorium;
import ar.com.platea.domain.Cinema;
import ar.com.platea.domain.Format;
import ar.com.platea.domain.Genre;
import ar.com.platea.domain.Language;
import ar.com.platea.domain.Movie;
import ar.com.platea.domain.Rating;
import ar.com.platea.domain.SeatReservation;
import ar.com.platea.domain.SeatStatus;
import ar.com.platea.domain.Showtime;
import ar.com.platea.repo.CinemaRepository;
import ar.com.platea.repo.MovieRepository;
import ar.com.platea.repo.SeatReservationRepository;
import ar.com.platea.repo.ShowtimeRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogSeeder {

	private static final Logger log = LoggerFactory.getLogger(CatalogSeeder.class);
	private static final int DAYS = 6;
	private static final int MIN_FREE_SEATS = 10;

	private final MovieRepository movies;
	private final CinemaRepository cinemas;
	private final ShowtimeRepository showtimes;
	private final SeatReservationRepository seats;
	private final Clock clock;

	public CatalogSeeder(
			MovieRepository movies,
			CinemaRepository cinemas,
			ShowtimeRepository showtimes,
			SeatReservationRepository seats,
			Clock clock) {
		this.movies = movies;
		this.cinemas = cinemas;
		this.showtimes = showtimes;
		this.seats = seats;
		this.clock = clock;
	}

	@Transactional
	public void seed() {
		LocalDate today = LocalDate.now(clock);
		Map<String, Movie> movieByTitle = seedMovies(today);
		Map<String, Auditorium> rooms = seedCinemas();
		List<Showtime> scheduled = schedule(today, movieByTitle, rooms);
		showtimes.saveAll(scheduled);
		showtimes.flush();
		List<SeatReservation> sold = new ArrayList<>();
		for (Showtime showtime : scheduled) {
			sold.addAll(soldSeats(showtime));
		}
		seats.saveAll(sold);
		log.info("Platea lista: {} películas, {} funciones, {} butacas ocupadas", movieByTitle.size(), scheduled.size(), sold.size());
	}

	private Map<String, Movie> seedMovies(LocalDate today) {
		List<MovieSpec> specs = List.of(
				new MovieSpec(
						"El último verano",
						"Una casa frente al mar y el verano que no vuelve.",
						"Después de vender la casa de veraneo, Nina vuelve a Mar del Plata por una sola semana para vaciarla. Entre cajones de fotos y un hermano que no atiende el teléfono, el pueblo se le presenta distinto: más chico, más caro, y lleno de gente que recuerda a su madre mejor que ella.",
						118, Genre.DRAMA, Rating.PLUS_13, "Mariana Heller", "Lucía Prieto, Tomás Almada, Vera Kohn",
						today.minusDays(21), true, true, "#16324f", "#e4c99a", "mar"),
				new MovieSpec(
						"Sombras del subte",
						"Hay una estación que no figura en el mapa.",
						"Un guardia de la línea B empieza a anotar los vagones que llegan con un pasajero de más. Nadie le cree, hasta que esa persona baja en una estación clausurada desde 1994 y lo espera en el andén. Thriller seco, nocturno, filmado casi entero bajo tierra.",
						106, Genre.THRILLER, Rating.PLUS_16, "Iván Ferreyra", "Ramiro Peña, Julieta Sosa, H. Kim",
						today.minusDays(14), true, false, "#141418", "#e25b45", "subte"),
				new MovieSpec(
						"Nebulosa",
						"La señal llegó tres minutos antes del apagón.",
						"En un observatorio de la puna, un equipo recibe un patrón que no debería existir: la voz de alguien que está en la sala, grabada con doce horas de anticipación. Cuando el cielo se apaga sobre el norte argentino, la única copia de la señal queda en un disco que no pueden sacar del valle.",
						141, Genre.CIENCIA_FICCION, Rating.PLUS_13, "Carla Ocampo", "Elena Duarte, Mateo Ruiz, Aisha Beltrán",
						today.minusDays(7), true, false, "#140e28", "#8ef0df", "espacio"),
				new MovieSpec(
						"La casa de los jacarandás",
						"Cinco primos, un árbol y una casa que no se vende.",
						"La tía Rosa deja en herencia una casona de Flores con una condición: los primos tienen que vivir juntos treinta días sin llamar a un martillero. Hay un gato que no es de nadie, una pared que esconde un quincho y la peor idea de negocio familiar desde el videoclub.",
						99, Genre.COMEDIA, Rating.ATP, "Pedro Lanz", "Mónica Vidal, Franco Ibarra, Lola Méndez, Ciro Paz",
						today.minusDays(40), true, false, "#1e3d2c", "#f0c95a", "arbol"),
				new MovieSpec(
						"Medianoche en Palermo",
						"Se conocieron en un cine que ya no existe.",
						"Inés restaura películas. Bruno pone en venta el cine de barrio donde ella vio todo lo que importa. Tienen diez noches de funciones privadas antes de la demolición y una sola copia en 35 mm que se niega a terminar de quemarse.",
						111, Genre.ROMANCE, Rating.PLUS_13, "Sofía Beltrán", "Paula Neri, Diego Alvez",
						today.minusDays(10), true, false, "#3b1d2e", "#f3b4c4", "noche"),
				new MovieSpec(
						"Código Austral",
						"El convoy sale de Ushuaia antes del amanecer.",
						"Una escolta improvisada tiene que cruzar la isla con un camión que no puede detenerse y un código que vence al llegar a Río Grande. Viento, hielo y una ruta de un solo carril. Acción filmada en Tierra del Fuego, sin superhéroes y con frío de verdad.",
						127, Genre.ACCION, Rating.PLUS_16, "Martín Ruiz", "Andrés Costa, Valentina Oro, Lee Park",
						today.minusDays(3), true, false, "#1b2430", "#e36b32", "convoy"),
				new MovieSpec(
						"Órbitas",
						"Un satélite viejo y el mapa del cielo.",
						"Lina, de once años, encuentra en el galpón de su abuelo la antena que sigue hablando con un satélite dado de baja. Juntos tienen que devolverlo a casa antes de que caiga sobre el pueblo. Animación luminosa, con humor y una banda sonora para mirar para arriba.",
						94, Genre.ANIMACION, Rating.ATP, "Estudio Bruma", "Voces de Mora Finzi, Coco Sosa, Rita Palma",
						today.minusDays(30), true, false, "#16365c", "#f2c14e", "orbita"),
				new MovieSpec(
						"La orilla",
						"El río devuelve lo que se lleva.",
						"En un pueblo del litoral, la creciente se retira y deja en la costa un bote que los vecinos habían visto hundirse veinte años atrás. Adentro no hay nadie. Igual, de noche, alguien amarra el cabo. Terror contenido: más humedad que grito.",
						102, Genre.TERROR, Rating.PLUS_16, "Elena Varela", "Nora Iglesias, Pablo Demaría",
						today.minusDays(5), true, false, "#121816", "#8eae98", "rio"),
				new MovieSpec(
						"Archivo 1978",
						"Las cintas que nadie debía escuchar.",
						"Una investigadora encuentra en un altillo de Tribunales una caja de cassettes sin inventario. Las voces son de una radio clandestina que salió al aire solo cuatro noches. El documental reconstruye quién grabó, quién escondió y por qué alguien sigue pidiendo que no se escuchen.",
						108, Genre.DOCUMENTAL, Rating.PLUS_13, "Nora Campos", "Documental",
						today.minusDays(18), true, false, "#2c261c", "#e0c097", "archivo"),
				new MovieSpec(
						"Viento norte",
						"Cruzar la cordillera no era el plan.",
						"Tres hermanas salen de Mendoza para esparcir las cenizas de su padre del otro lado de los Andes. El paso se cierra, el auto no da más y la menor confiesa para qué quería hacer el viaje de verdad. Aventura íntima, de ruta y de familia.",
						124, Genre.AVENTURA, Rating.ATP, "Hugo Salinas", "Irene Lago, Carla Mena, Luz Vidal",
						today.plusDays(21), false, false, "#243044", "#d7e2ea", "cordillera"),
				new MovieSpec(
						"El faro de Quequén",
						"La luz se apagó y el pueblo no llamó a nadie.",
						"Un farero joven llega a un pueblo que ya no espera barcos. La mujer que le alquila la pieza dice que el faro sigue girando aunque esté a oscuras. Drama chico, de viento y de gente que se queda.",
						116, Genre.DRAMA, Rating.PLUS_13, "Luis Pereyra", "Joaquín Abril, Helena Costa",
						today.plusDays(35), false, false, "#1d2a33", "#f2efe6", "faro"));

		Map<String, Movie> byTitle = new HashMap<>();
		int order = 0;
		for (MovieSpec spec : specs) {
			Movie movie = new Movie();
			movie.setTitle(spec.title());
			movie.setSlug(Slugs.slug(spec.title()));
			movie.setTagline(spec.tagline());
			movie.setSynopsis(spec.synopsis());
			movie.setRuntimeMinutes(spec.runtime());
			movie.setGenre(spec.genre());
			movie.setRating(spec.rating());
			movie.setDirector(spec.director());
			movie.setCastNames(spec.cast());
			movie.setReleaseDate(spec.release());
			movie.setNowShowing(spec.nowShowing());
			movie.setFeatured(spec.featured());
			movie.setPosterBg(spec.bg());
			movie.setPosterAccent(spec.accent());
			movie.setMotif(spec.motif());
			movie.setSortOrder(order++);
			byTitle.put(spec.title(), movies.save(movie));
		}
		return byTitle;
	}

	private Map<String, Auditorium> seedCinemas() {
		record RoomSpec(String name, int rows, int seats) {
		}
		record CinemaSpec(String name, String city, String neighborhood, String address, List<RoomSpec> rooms) {
		}
		List<CinemaSpec> specs = List.of(
				new CinemaSpec("Platea Palermo", "Buenos Aires", "Palermo", "Av. Santa Fe 3253",
						List.of(new RoomSpec("Sala 1", 8, 12), new RoomSpec("Sala 2", 8, 12), new RoomSpec("Sala IMAX", 9, 14))),
				new CinemaSpec("Platea Puerto", "Buenos Aires", "Puerto Madero", "Juana Manso 980",
						List.of(new RoomSpec("Sala 1", 8, 12), new RoomSpec("Sala 4DX", 7, 10), new RoomSpec("Sala 3", 8, 12))),
				new CinemaSpec("Platea Belgrano", "Buenos Aires", "Belgrano", "Av. Cabildo 2120",
						List.of(new RoomSpec("Sala 1", 8, 10), new RoomSpec("Sala 2", 8, 10))),
				new CinemaSpec("Platea Nueva Córdoba", "Córdoba", "Nueva Córdoba", "Obispo Trejo 650",
						List.of(new RoomSpec("Sala 1", 8, 12), new RoomSpec("Sala IMAX", 9, 14), new RoomSpec("Sala 3", 7, 12))),
				new CinemaSpec("Platea Rosario", "Rosario", "Centro", "Córdoba 1340",
						List.of(new RoomSpec("Sala 1", 8, 12), new RoomSpec("Sala 2", 8, 10))),
				new CinemaSpec("Platea Mendoza", "Mendoza", "Centro", "Av. San Martín 845",
						List.of(new RoomSpec("Sala 1", 7, 12), new RoomSpec("Sala 2", 7, 10))));

		Map<String, Auditorium> rooms = new HashMap<>();
		int order = 0;
		for (CinemaSpec spec : specs) {
			Cinema cinema = new Cinema();
			cinema.setName(spec.name());
			cinema.setSlug(Slugs.slug(spec.name()));
			cinema.setCity(spec.city());
			cinema.setNeighborhood(spec.neighborhood());
			cinema.setAddress(spec.address());
			cinema.setSortOrder(order++);
			for (RoomSpec roomSpec : spec.rooms()) {
				Auditorium room = new Auditorium();
				room.setCinema(cinema);
				room.setName(roomSpec.name());
				room.setRowCount(roomSpec.rows());
				room.setSeatsPerRow(roomSpec.seats());
				cinema.getAuditoriums().add(room);
				rooms.put(cinema.getSlug() + "|" + room.getName(), room);
			}
			cinemas.save(cinema);
		}
		return rooms;
	}

	private List<Showtime> schedule(LocalDate today, Map<String, Movie> movieByTitle, Map<String, Auditorium> rooms) {
		List<Slot> slots = List.of(
				slot("El último verano", "platea-palermo", "Sala 1", Language.SUB, Format.DOS_D, 14, 30),
				slot("Sombras del subte", "platea-palermo", "Sala 1", Language.SUB, Format.DOS_D, 17, 0),
				slot("Medianoche en Palermo", "platea-palermo", "Sala 1", Language.SUB, Format.DOS_D, 19, 30),
				slot("La orilla", "platea-palermo", "Sala 1", Language.SUB, Format.DOS_D, 22, 0),
				slot("Órbitas", "platea-palermo", "Sala 2", Language.DUB, Format.TRES_D, 14, 45),
				slot("Órbitas", "platea-palermo", "Sala 2", Language.SUB, Format.TRES_D, 17, 10),
				slot("Nebulosa", "platea-palermo", "Sala 2", Language.SUB, Format.TRES_D, 19, 20),
				slot("Código Austral", "platea-palermo", "Sala 2", Language.SUB, Format.TRES_D, 22, 20),
				slot("Nebulosa", "platea-palermo", "Sala IMAX", Language.SUB, Format.IMAX, 15, 0),
				slot("Nebulosa", "platea-palermo", "Sala IMAX", Language.SUB, Format.IMAX, 18, 0),
				slot("Código Austral", "platea-palermo", "Sala IMAX", Language.SUB, Format.IMAX, 21, 10),
				slot("La casa de los jacarandás", "platea-puerto", "Sala 1", Language.SUB, Format.DOS_D, 14, 20),
				slot("Archivo 1978", "platea-puerto", "Sala 1", Language.SUB, Format.DOS_D, 16, 40),
				slot("El último verano", "platea-puerto", "Sala 1", Language.SUB, Format.DOS_D, 19, 15),
				slot("Sombras del subte", "platea-puerto", "Sala 1", Language.SUB, Format.DOS_D, 21, 45),
				slot("Código Austral", "platea-puerto", "Sala 4DX", Language.SUB, Format.CUATRO_DX, 16, 0),
				slot("Código Austral", "platea-puerto", "Sala 4DX", Language.SUB, Format.CUATRO_DX, 19, 0),
				slot("Código Austral", "platea-puerto", "Sala 4DX", Language.OV, Format.CUATRO_DX, 21, 50),
				slot("Órbitas", "platea-puerto", "Sala 3", Language.DUB, Format.TRES_D, 15, 0),
				slot("Órbitas", "platea-puerto", "Sala 3", Language.SUB, Format.TRES_D, 17, 30),
				slot("Nebulosa", "platea-puerto", "Sala 3", Language.SUB, Format.TRES_D, 20, 0),
				slot("Código Austral", "platea-puerto", "Sala 3", Language.SUB, Format.TRES_D, 22, 50),
				slot("La casa de los jacarandás", "platea-belgrano", "Sala 1", Language.SUB, Format.DOS_D, 15, 30),
				slot("El último verano", "platea-belgrano", "Sala 1", Language.SUB, Format.DOS_D, 18, 0),
				slot("Medianoche en Palermo", "platea-belgrano", "Sala 1", Language.SUB, Format.DOS_D, 20, 40),
				slot("La orilla", "platea-belgrano", "Sala 1", Language.SUB, Format.DOS_D, 23, 0),
				slot("Órbitas", "platea-belgrano", "Sala 2", Language.DUB, Format.TRES_D, 16, 0),
				slot("Nebulosa", "platea-belgrano", "Sala 2", Language.SUB, Format.TRES_D, 18, 30),
				slot("Código Austral", "platea-belgrano", "Sala 2", Language.SUB, Format.TRES_D, 21, 20),
				slot("El último verano", "platea-nueva-cordoba", "Sala 1", Language.SUB, Format.DOS_D, 15, 0),
				slot("La casa de los jacarandás", "platea-nueva-cordoba", "Sala 1", Language.SUB, Format.DOS_D, 17, 40),
				slot("Sombras del subte", "platea-nueva-cordoba", "Sala 1", Language.SUB, Format.DOS_D, 20, 0),
				slot("Medianoche en Palermo", "platea-nueva-cordoba", "Sala 1", Language.SUB, Format.DOS_D, 22, 20),
				slot("Nebulosa", "platea-nueva-cordoba", "Sala IMAX", Language.SUB, Format.IMAX, 16, 30),
				slot("Código Austral", "platea-nueva-cordoba", "Sala IMAX", Language.SUB, Format.IMAX, 19, 30),
				slot("Nebulosa", "platea-nueva-cordoba", "Sala IMAX", Language.SUB, Format.IMAX, 22, 15),
				slot("Órbitas", "platea-nueva-cordoba", "Sala 3", Language.DUB, Format.TRES_D, 15, 20),
				slot("Órbitas", "platea-nueva-cordoba", "Sala 3", Language.SUB, Format.TRES_D, 18, 0),
				slot("Código Austral", "platea-nueva-cordoba", "Sala 3", Language.SUB, Format.TRES_D, 20, 40),
				slot("El último verano", "platea-rosario", "Sala 1", Language.SUB, Format.DOS_D, 16, 0),
				slot("Archivo 1978", "platea-rosario", "Sala 1", Language.SUB, Format.DOS_D, 18, 40),
				slot("La orilla", "platea-rosario", "Sala 1", Language.SUB, Format.DOS_D, 21, 10),
				slot("Órbitas", "platea-rosario", "Sala 2", Language.DUB, Format.TRES_D, 15, 40),
				slot("Nebulosa", "platea-rosario", "Sala 2", Language.SUB, Format.TRES_D, 18, 10),
				slot("Código Austral", "platea-rosario", "Sala 2", Language.SUB, Format.TRES_D, 21, 5),
				slot("La casa de los jacarandás", "platea-mendoza", "Sala 1", Language.SUB, Format.DOS_D, 16, 30),
				slot("El último verano", "platea-mendoza", "Sala 1", Language.SUB, Format.DOS_D, 18, 50),
				slot("Sombras del subte", "platea-mendoza", "Sala 1", Language.SUB, Format.DOS_D, 21, 30),
				slot("Órbitas", "platea-mendoza", "Sala 2", Language.DUB, Format.TRES_D, 17, 0),
				slot("Nebulosa", "platea-mendoza", "Sala 2", Language.SUB, Format.TRES_D, 19, 20),
				slot("Código Austral", "platea-mendoza", "Sala 2", Language.SUB, Format.TRES_D, 22, 15));

		List<Showtime> scheduled = new ArrayList<>();
		for (int day = 0; day < DAYS; day++) {
			LocalDate date = today.plusDays(day);
			for (Slot slot : slots) {
				Movie movie = movieByTitle.get(slot.movie());
				Auditorium room = rooms.get(slot.cinema() + "|" + slot.room());
				if (movie == null || room == null) {
					throw new IllegalStateException("Falta " + slot.movie() + " en " + slot.cinema() + " " + slot.room());
				}
				Showtime showtime = new Showtime();
				showtime.setMovie(movie);
				showtime.setAuditorium(room);
				showtime.setStartsAt(date.atTime(slot.hour(), slot.minute()));
				showtime.setFormat(slot.format());
				showtime.setLanguage(slot.language());
				showtime.setBasePrice(Pricing.base(slot.format(), date.getDayOfWeek()));
				showtime.setPremiumSurcharge(Pricing.PREMIUM_SURCHARGE);
				scheduled.add(showtime);
			}
		}
		return scheduled;
	}

	private List<SeatReservation> soldSeats(Showtime showtime) {
		Auditorium room = showtime.getAuditorium();
		int occupancy = occupancy(showtime);
		int maxSold = Math.max(0, room.capacity() - MIN_FREE_SEATS);
		List<String> candidates = new ArrayList<>();
		for (int row = 0; row < room.getRowCount(); row++) {
			for (int number = 1; number <= room.getSeatsPerRow(); number++) {
				String code = SeatLayout.rowLabel(row) + number;
				int hash = Math.floorMod(Objects.hash(showtime.getId(), code), 100);
				if (hash < occupancy) {
					candidates.add(code);
				}
			}
		}
		if (candidates.size() > maxSold) {
			candidates.sort(Comparator.comparingInt(code -> Math.floorMod(Objects.hash(showtime.getId(), code, 17), 10_000)));
			candidates = new ArrayList<>(candidates.subList(0, maxSold));
		}
		List<SeatReservation> sold = new ArrayList<>();
		for (String code : candidates) {
			SeatLayout.Position position = SeatLayout.parse(room, code);
			SeatReservation seat = new SeatReservation();
			seat.setShowtime(showtime);
			seat.setSeatCode(code);
			seat.setRowLabel(position.rowLabel());
			seat.setSeatNumber(position.number());
			seat.setPremium(position.premium());
			seat.setPrice(showtime.getBasePrice() + (position.premium() ? showtime.getPremiumSurcharge() : 0));
			seat.setStatus(SeatStatus.SOLD);
			sold.add(seat);
		}
		return sold;
	}

	private int occupancy(Showtime showtime) {
		long hours = Duration.between(LocalDateTime.now(clock), showtime.getStartsAt()).toHours();
		int occupancy;
		if (hours < 8) {
			occupancy = 50;
		} else if (hours < 30) {
			occupancy = 34;
		} else if (hours < 72) {
			occupancy = 20;
		} else {
			occupancy = 12;
		}
		String title = showtime.getMovie().getTitle();
		if (title.equals("Nebulosa") || title.equals("Código Austral")) {
			occupancy += 8;
		}
		return Math.min(occupancy, 62);
	}

	private static Slot slot(String movie, String cinema, String room, Language language, Format format, int hour, int minute) {
		return new Slot(movie, cinema, room, language, format, hour, minute);
	}

	private record MovieSpec(
			String title,
			String tagline,
			String synopsis,
			int runtime,
			Genre genre,
			Rating rating,
			String director,
			String cast,
			LocalDate release,
			boolean nowShowing,
			boolean featured,
			String bg,
			String accent,
			String motif) {
	}

	private record Slot(String movie, String cinema, String room, Language language, Format format, int hour, int minute) {
	}
}
