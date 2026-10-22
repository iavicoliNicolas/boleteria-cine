package ar.com.platea.view;

import java.util.List;

public record CinemaGroup(String name, String slug, String meta, List<ShowtimeItem> showtimes) {
}
