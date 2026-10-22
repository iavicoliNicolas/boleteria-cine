package ar.com.platea.web;

import ar.com.platea.service.Dates;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.Year;
import java.util.List;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalAdvice {

	public static final String CITY = "platea.ciudad";
	public static final List<String> CITIES = List.of("Buenos Aires", "Córdoba", "Rosario", "Mendoza");

	@ModelAttribute("ciudadActiva")
	public String ciudadActiva(HttpServletRequest request) {
		HttpSession session = request.getSession();
		if (request.getParameterMap().containsKey("ciudad")) {
			String value = request.getParameter("ciudad");
			String canonical = CITIES.stream()
					.filter(city -> city.equalsIgnoreCase(value == null ? "" : value.trim()))
					.findFirst()
					.orElse(null);
			if (canonical == null) {
				session.removeAttribute(CITY);
			} else {
				session.setAttribute(CITY, canonical);
			}
		}
		return (String) session.getAttribute(CITY);
	}

	@ModelAttribute("ciudades")
	public List<String> ciudades() {
		return CITIES;
	}

	@ModelAttribute("ruta")
	public String ruta(HttpServletRequest request) {
		return request.getRequestURI();
	}

	@ModelAttribute("anio")
	public int anio() {
		return Year.now(Dates.ZONE).getValue();
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ModelAndView status(ResponseStatusException exception) {
		ModelAndView model = new ModelAndView("error");
		model.setStatus(exception.getStatusCode());
		model.addObject("status", exception.getStatusCode().value());
		return model;
	}

	public static String city(HttpSession session) {
		Object value = session.getAttribute(CITY);
		return value instanceof String city ? city : null;
	}
}
