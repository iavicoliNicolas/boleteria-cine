package ar.com.platea.view;

public record ShowtimeItem(
		long id,
		String time,
		String format,
		String formatKey,
		String language,
		String auditorium,
		String price,
		boolean soldOut) {
}
