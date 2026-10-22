package ar.com.platea.view;

import java.util.List;

public record MoviePage(
		MovieInfo info,
		List<DayChip> days,
		String selectedLabel,
		List<CinemaGroup> groups,
		String banner,
		boolean schedule) {

	public record MovieInfo(
			String slug,
			String title,
			String tagline,
			String synopsis,
			String genre,
			String rating,
			String ratingKey,
			String ratingHint,
			String runtime,
			String director,
			String cast,
			String release,
			String posterBg,
			String posterAccent,
			String motif,
			boolean nowShowing) {
	}
}
