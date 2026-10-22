package ar.com.platea.view;

import java.util.List;

public record HomeView(
		MovieCard featured,
		List<MovieCard> cartelera,
		List<MovieCard> proximamente,
		List<RailItem> rail,
		String railLabel,
		String query,
		String genre,
		List<GenreOption> genres,
		boolean filtrando,
		String cityLabel,
		int total) {

	public record MovieCard(
			String slug,
			String title,
			String tagline,
			String genre,
			String rating,
			String ratingKey,
			String ratingHint,
			String runtime,
			String director,
			String posterBg,
			String posterAccent,
			String motif,
			String nextShow,
			String priceFrom,
			boolean available) {
	}

	public record RailItem(
			long showtimeId,
			String time,
			String title,
			String place,
			String format,
			String formatKey,
			String slug) {
	}

	public record GenreOption(String value, String label, boolean selected) {
	}
}
