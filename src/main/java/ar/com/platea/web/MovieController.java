package ar.com.platea.web;

import ar.com.platea.service.CatalogService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MovieController {

	private final CatalogService catalog;

	public MovieController(CatalogService catalog) {
		this.catalog = catalog;
	}

	@GetMapping("/peliculas/{slug}")
	public String movie(
			@PathVariable String slug,
			@RequestParam(name = "fecha", required = false) String fecha,
			HttpSession session,
			Model model) {
		model.addAttribute("movie", catalog.movie(slug, parseDate(fecha), GlobalAdvice.city(session)));
		return "pelicula";
	}

	private LocalDate parseDate(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return LocalDate.parse(raw);
		} catch (DateTimeParseException ex) {
			return null;
		}
	}
}
