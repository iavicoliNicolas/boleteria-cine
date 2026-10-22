package ar.com.platea.web;

import ar.com.platea.service.CatalogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

	private final CatalogService catalog;

	public HomeController(CatalogService catalog) {
		this.catalog = catalog;
	}

	@GetMapping("/")
	public String home(
			HttpSession session,
			@RequestParam(name = "q", required = false) String query,
			@RequestParam(name = "genero", required = false) String genre,
			Model model) {
		model.addAttribute("home", catalog.home(GlobalAdvice.city(session), query, genre));
		return "index";
	}
}
