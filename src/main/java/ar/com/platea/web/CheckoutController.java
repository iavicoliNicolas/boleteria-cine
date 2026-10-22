package ar.com.platea.web;

import ar.com.platea.service.BookingException;
import ar.com.platea.service.BookingService;
import ar.com.platea.service.CardRejectedException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CheckoutController {

	private final BookingService bookings;

	public CheckoutController(BookingService bookings) {
		this.bookings = bookings;
	}

	@GetMapping("/checkout")
	public String form(HttpSession session, Model model) {
		if (!model.containsAttribute("form")) {
			model.addAttribute("form", new CheckoutForm());
		}
		model.addAttribute("compra", bookings.preview(session.getId()).orElse(null));
		return "checkout";
	}

	@PostMapping("/checkout")
	public String pay(
			@Valid @ModelAttribute("form") CheckoutForm form,
			BindingResult binding,
			HttpSession session,
			Model model) {
		if (!binding.hasErrors()) {
			try {
				var booking = bookings.confirm(form, session.getId());
				return "redirect:/entradas/" + booking.getCode();
			} catch (CardRejectedException ex) {
				binding.rejectValue(ex.field(), "card", ex.getMessage());
			} catch (BookingException ex) {
				model.addAttribute("errorReserva", ex.getMessage());
			}
		}
		model.addAttribute("compra", bookings.preview(session.getId()).orElse(null));
		return "checkout";
	}

	@PostMapping("/checkout/soltar")
	public String release(@RequestParam(required = false) Long funcion, HttpSession session) {
		bookings.release(session.getId());
		if (funcion != null) {
			return "redirect:/funciones/" + funcion;
		}
		return "redirect:/";
	}
}
