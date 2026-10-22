package ar.com.platea.web;

import ar.com.platea.service.BookingService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class TicketController {

	private final BookingService bookings;

	public TicketController(BookingService bookings) {
		this.bookings = bookings;
	}

	@GetMapping("/entradas/{code}")
	public String ticket(@PathVariable String code, Model model) {
		model.addAttribute("entrada", bookings.ticket(code).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
		return "entrada";
	}

	@GetMapping("/mis-entradas")
	public String search(@RequestParam(name = "q", required = false) String query, Model model) {
		String q = query == null ? "" : query.trim();
		model.addAttribute("query", q);
		if (q.isBlank()) {
			model.addAttribute("resultados", List.of());
			return "mis-entradas";
		}
		if (q.contains("@")) {
			var results = bookings.byEmail(q);
			model.addAttribute("resultados", results);
			if (results.isEmpty()) {
				model.addAttribute("mensaje", "No encontramos entradas con ese email.");
			}
			return "mis-entradas";
		}
		var ticket = bookings.ticket(q);
		if (ticket.isPresent()) {
			return "redirect:/entradas/" + ticket.get().code();
		}
		model.addAttribute("resultados", List.of());
		model.addAttribute("mensaje", "No encontramos una entrada con ese código.");
		return "mis-entradas";
	}
}
