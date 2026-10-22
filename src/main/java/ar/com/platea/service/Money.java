package ar.com.platea.service;

import java.util.Locale;

public final class Money {

	private Money() {
	}

	public static String pesos(int amount) {
		String grouped = String.format(Locale.US, "%,d", amount).replace(',', '.');
		return "$ " + grouped;
	}
}
