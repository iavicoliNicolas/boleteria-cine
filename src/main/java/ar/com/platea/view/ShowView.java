package ar.com.platea.view;

import java.util.List;

public record ShowView(
		long id,
		String slug,
		String title,
		String cinema,
		String cinemaSlug,
		String neighborhood,
		String address,
		String city,
		String auditorium,
		String when,
		String dateIso,
		String format,
		String formatKey,
		String language,
		String rating,
		String ratingKey,
		List<SeatRow> rows,
		int selectedCount,
		String selectedTotal,
		boolean closed,
		Long expiresAt,
		int holdMinutes) {

	public record SeatRow(String letter, List<List<SeatCell>> blocks) {
	}

	public record SeatCell(
			String code,
			int number,
			boolean premium,
			int price,
			String priceText,
			String state) {
	}
}
