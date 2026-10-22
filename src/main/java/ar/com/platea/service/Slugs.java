package ar.com.platea.service;

import java.text.Normalizer;
import java.util.Locale;

public final class Slugs {

	private Slugs() {
	}

	public static String slug(String input) {
		return fold(input).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
	}

	public static String fold(String input) {
		if (input == null || input.isBlank()) {
			return "";
		}
		return Normalizer.normalize(input, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.toLowerCase(Locale.ROOT);
	}
}
