package ar.com.platea.web;

import ar.com.platea.service.CatalogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CinemaController {

	private final CatalogService catalog;

	public CinemaController(CatalogService catalog) {
		this.catalog = catalog;
	}

	@GetMapping("/complejos")
	public String list(HttpSession session, Model model) {
		String city = GlobalAdvice.city(session);
		model.addAttribute("complejos", catalog.cinemas(city));
		model.addAttribute("cityLabel", city == null ? "Todas las ciudades" : city);
		return "complejos";
	}

	@GetMapping("/complejos/{slug}")
	public String detail(@PathVariable String slug, Model model) {
		model.addAttribute("complejo", catalog.cinema(slug));
		return "complejo";
	}
}
