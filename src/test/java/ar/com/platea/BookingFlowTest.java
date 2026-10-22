package ar.com.platea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.com.platea.domain.Showtime;
import ar.com.platea.repo.SeatReservationRepository;
import ar.com.platea.repo.ShowtimeRepository;
import ar.com.platea.service.SeatLayout;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class BookingFlowTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private Clock clock;

	@Autowired
	private ShowtimeRepository showtimes;

	@Autowired
	private SeatReservationRepository seats;

	@Test
	void laCarteleraMuestraPeliculasComplejosYEstrenos() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("El último verano")))
				.andExpect(content().string(containsString("Próximamente")))
				.andExpect(content().string(containsString("Viento norte")));

		mockMvc.perform(get("/").param("ciudad", "Mendoza"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Medianoche en Palermo")))
				.andExpect(content().string(containsString("Sin funciones en esta ciudad")));

		mockMvc.perform(get("/").param("genero", "ANIMACION"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Órbitas")))
				.andExpect(content().string(not(containsString("Sombras del subte"))));

		mockMvc.perform(get("/peliculas/el-ultimo-verano"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Mariana Heller")))
				.andExpect(content().string(containsString("Platea Palermo")));

		mockMvc.perform(get("/peliculas/viento-norte"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Todavía no hay funciones")));

		mockMvc.perform(get("/peliculas/no-existe"))
				.andExpect(status().isNotFound())
				.andExpect(content().string(containsString("no está en la cartelera")));

		mockMvc.perform(get("/complejos"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Platea Rosario")));

		mockMvc.perform(get("/complejos/platea-palermo"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Santa Fe")));

		mockMvc.perform(get("/checkout"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("No tenés butacas")));
	}

	@Test
	void reservaButacasYEmiteLaEntrada() throws Exception {
		Showtime show = upcoming();
		List<String> codes = freeCodes(show, 2);
		MockHttpSession session = new MockHttpSession();

		mockMvc.perform(get("/funciones/" + show.getId()).session(session))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString(show.getMovie().getTitle())))
				.andExpect(content().string(containsString("Pantalla")));

		mockMvc.perform(post("/funciones/" + show.getId() + "/retener").session(session).param("butacas", codes.get(0), codes.get(1)))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/checkout"));

		MvcResult paid = mockMvc.perform(post("/checkout").session(session)
						.param("nombre", "Ana Pérez")
						.param("email", "ana.flow@platea.test")
						.param("telefono", "1130001122")
						.param("tarjeta", "4242424242424242")
						.param("vencimiento", "12/30")
						.param("cvv", "123")
						.param("titular", "ANA PEREZ"))
				.andExpect(status().is3xxRedirection())
				.andReturn();

		String location = paid.getResponse().getRedirectedUrl();
		assertThat(location).startsWith("/entradas/PLT-");
		String code = location.substring("/entradas/".length());

		mockMvc.perform(get(location))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString(code)))
				.andExpect(content().string(containsString(show.getMovie().getTitle())))
				.andExpect(content().string(containsString(codes.get(0))))
				.andExpect(content().string(containsString("4242")))
				.andExpect(content().string(not(containsString("4242424242424242"))));

		mockMvc.perform(get("/mis-entradas").param("q", "ana.flow@platea.test"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString(show.getMovie().getTitle())));

		mockMvc.perform(get("/mis-entradas").param("q", code))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(location));
	}

	@Test
	void noDejaVenderDosVecesLaMismaButaca() throws Exception {
		Showtime show = upcoming();
		String code = freeCodes(show, 1).get(0);
		MockHttpSession first = new MockHttpSession();
		MockHttpSession second = new MockHttpSession();

		mockMvc.perform(post("/funciones/" + show.getId() + "/retener").session(first).param("butacas", code))
				.andExpect(redirectedUrl("/checkout"));

		mockMvc.perform(post("/funciones/" + show.getId() + "/retener").session(second).param("butacas", code))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/funciones/" + show.getId()))
				.andExpect(flash().attributeExists("error"));
	}

	@Test
	void rechazaLaTarjetaTerminadaEnCerosYMantieneLaReserva() throws Exception {
		Showtime show = upcoming();
		String code = freeCodes(show, 1).get(0);
		MockHttpSession session = new MockHttpSession();

		mockMvc.perform(post("/funciones/" + show.getId() + "/retener").session(session).param("butacas", code))
				.andExpect(redirectedUrl("/checkout"));

		mockMvc.perform(post("/checkout").session(session)
						.param("nombre", "Leo Díaz")
						.param("email", "leo@platea.test")
						.param("tarjeta", "4000000000020000")
						.param("vencimiento", "12/30")
						.param("cvv", "123")
						.param("titular", "LEO DIAZ"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("rechazó")));

		mockMvc.perform(get("/checkout").session(session))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString(show.getMovie().getTitle())))
				.andExpect(content().string(containsString("Pagar y emitir entradas")));
	}

	private Showtime upcoming() {
		LocalDateTime now = LocalDateTime.now(clock);
		for (Showtime show : showtimes.findBetween(now.plusHours(2), now.plusDays(3))) {
			if (freeCodes(show, 4).size() >= 4) {
				return show;
			}
		}
		throw new AssertionError("No hay una función con butacas libres");
	}

	private List<String> freeCodes(Showtime show, int needed) {
		Set<String> taken = seats.findByShowtimeId(show.getId()).stream()
				.map(seat -> seat.getSeatCode())
				.collect(Collectors.toSet());
		List<String> free = new ArrayList<>();
		for (int row = 0; row < show.getAuditorium().getRowCount() && free.size() < needed; row++) {
			for (int number = 1; number <= show.getAuditorium().getSeatsPerRow() && free.size() < needed; number++) {
				String code = SeatLayout.rowLabel(row) + number;
				if (!taken.contains(code)) {
					free.add(code);
				}
			}
		}
		assertThat(free).hasSize(needed);
		return free;
	}
}
