package ar.com.platea.web;

import ar.com.platea.service.BookingException;
import ar.com.platea.service.BookingService;
import ar.com.platea.service.CatalogService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ShowtimeController {

	private final CatalogService catalog;
	private final BookingService bookings;

	public ShowtimeController(CatalogService catalog, BookingService bookings) {
		this.catalog = catalog;
		this.bookings = bookings;
	}

	@GetMapping("/funciones/{id}")
	public String show(@PathVariable long id, HttpSession session, Model model) {
		model.addAttribute("funcion", catalog.showtime(id, session.getId()));
		return "funcion";
	}

	@PostMapping("/funciones/{id}/retener")
	public String hold(
			@PathVariable long id,
			@RequestParam(name = "butacas", required = false) List<String> seats,
			HttpSession session,
			RedirectAttributes redirect) {
		try {
			bookings.hold(id, seats, session.getId());
			return "redirect:/checkout";
		} catch (BookingException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
			return "redirect:/funciones/" + id;
		}
	}
}
