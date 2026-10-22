package ar.com.platea.view;

import java.util.List;

public record CheckoutView(
		long showtimeId,
		String slug,
		String title,
		String cinema,
		String address,
		String when,
		String format,
		String language,
		List<Line> lines,
		String total,
		Long expiresAt,
		String until) {

	public record Line(String code, String label, String price, boolean premium) {
	}
}
