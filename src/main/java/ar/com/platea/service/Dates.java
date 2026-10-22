package ar.com.platea.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Dates {

	public static final ZoneId ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

	private static final String[] DAYS = {
		"lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo"
	};
	private static final String[] DAYS_SHORT = {"lun", "mar", "mié", "jue", "vie", "sáb", "dom"};
	private static final String[] MONTHS = {
		"enero", "febrero", "marzo", "abril", "mayo", "junio",
		"julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
	};
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

	private Dates() {
	}

	public static String chip(LocalDate date, LocalDate today) {
		if (date.equals(today)) {
			return "Hoy";
		}
		if (date.equals(today.plusDays(1))) {
			return "Mañana";
		}
		return cap(DAYS_SHORT[date.getDayOfWeek().getValue() - 1]) + " " + date.getDayOfMonth();
	}

	public static String longDate(LocalDate date) {
		return cap(DAYS[date.getDayOfWeek().getValue() - 1]) + " " + date.getDayOfMonth() + " de " + MONTHS[date.getMonthValue() - 1];
	}

	public static String when(LocalDateTime dateTime) {
		return longDate(dateTime.toLocalDate()) + " · " + dateTime.format(TIME);
	}

	public static String purchased(LocalDateTime dateTime) {
		return dateTime.getDayOfMonth() + " de " + MONTHS[dateTime.getMonthValue() - 1] + " de " + dateTime.getYear()
				+ " · " + dateTime.format(TIME);
	}

	public static String release(LocalDate date) {
		return date.getDayOfMonth() + " de " + MONTHS[date.getMonthValue() - 1];
	}

	public static String relativeTime(LocalDateTime dateTime, LocalDate today) {
		String time = dateTime.format(TIME);
		LocalDate date = dateTime.toLocalDate();
		if (date.equals(today)) {
			return "Hoy " + time;
		}
		if (date.equals(today.plusDays(1))) {
			return "Mañana " + time;
		}
		return chip(date, today) + " " + time;
	}

	public static String clock(LocalDateTime dateTime) {
		return dateTime.format(TIME);
	}

	private static String cap(String value) {
		return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
	}
}
