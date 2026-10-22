package ar.com.platea.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CardValidator {

	private static final Pattern EXPIRY = Pattern.compile("^(0[1-9]|1[0-2])/(\\d{2}|\\d{4})$");

	private CardValidator() {
	}

	public record Card(String brand, String last4) {
	}

	public static Card check(String pan, String expiry, String cvv, String holder, LocalDate today) {
		String digits = pan == null ? "" : pan.replaceAll("\\D", "");
		if (digits.length() < 13 || digits.length() > 19 || !luhn(digits)) {
			throw new CardRejectedException("tarjeta", "Revisá el número de la tarjeta.");
		}
		if (digits.endsWith("0000")) {
			throw new CardRejectedException("tarjeta", "El banco rechazó el pago. Probá con otra tarjeta.");
		}
		Matcher matcher = EXPIRY.matcher(expiry == null ? "" : expiry.trim());
		if (!matcher.matches()) {
			throw new CardRejectedException("vencimiento", "Usá el formato MM/AA.");
		}
		int month = Integer.parseInt(matcher.group(1));
		int year = Integer.parseInt(matcher.group(2));
		if (year < 100) {
			year += 2000;
		}
		if (YearMonth.of(year, month).isBefore(YearMonth.from(today))) {
			throw new CardRejectedException("vencimiento", "La tarjeta está vencida.");
		}
		if (cvv == null || !cvv.matches("\\d{3,4}")) {
			throw new CardRejectedException("cvv", "El código de seguridad tiene 3 o 4 dígitos.");
		}
		if (holder == null || holder.isBlank()) {
			throw new CardRejectedException("titular", "Ingresá el nombre del titular.");
		}
		return new Card(brand(digits), digits.substring(digits.length() - 4));
	}

	public static boolean luhn(String digits) {
		int sum = 0;
		boolean alternate = false;
		for (int index = digits.length() - 1; index >= 0; index--) {
			int digit = digits.charAt(index) - '0';
			if (digit < 0 || digit > 9) {
				return false;
			}
			if (alternate) {
				digit *= 2;
				if (digit > 9) {
					digit -= 9;
				}
			}
			sum += digit;
			alternate = !alternate;
		}
		return !digits.isEmpty() && sum % 10 == 0;
	}

	private static String brand(String digits) {
		if (digits.startsWith("34") || digits.startsWith("37")) {
			return "American Express";
		}
		if (digits.startsWith("4")) {
			return "Visa";
		}
		if (digits.startsWith("5") || digits.startsWith("2")) {
			return "Mastercard";
		}
		return "Tarjeta";
	}
}
