package ar.com.platea.view;

import java.util.List;

public record CinemaPage(
		String name,
		String city,
		String neighborhood,
		String address,
		String slug,
		int rooms,
		String windowLabel,
		List<CinemaMovie> movies) {

	public record CinemaMovie(
			String title,
			String slug,
			String rating,
			String ratingKey,
			String motif,
			String posterBg,
			String posterAccent,
			List<ShowtimeItem> showtimes) {
	}
}
