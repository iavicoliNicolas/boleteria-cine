package ar.com.platea.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "movies")
public class Movie {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, unique = true)
	private String slug;

	@Column(nullable = false)
	private String tagline;

	@Column(nullable = false, length = 2500)
	private String synopsis;

	@Column(nullable = false)
	private int runtimeMinutes;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Genre genre;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Rating rating;

	@Column(nullable = false)
	private String director;

	@Column(nullable = false)
	private String castNames;

	@Column(nullable = false)
	private LocalDate releaseDate;

	@Column(nullable = false)
	private boolean nowShowing;

	@Column(nullable = false)
	private boolean featured;

	@Column(nullable = false)
	private String posterBg;

	@Column(nullable = false)
	private String posterAccent;

	@Column(nullable = false)
	private String motif;

	@Column(nullable = false)
	private int sortOrder;

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getTagline() {
		return tagline;
	}

	public void setTagline(String tagline) {
		this.tagline = tagline;
	}

	public String getSynopsis() {
		return synopsis;
	}

	public void setSynopsis(String synopsis) {
		this.synopsis = synopsis;
	}

	public int getRuntimeMinutes() {
		return runtimeMinutes;
	}

	public void setRuntimeMinutes(int runtimeMinutes) {
		this.runtimeMinutes = runtimeMinutes;
	}

	public Genre getGenre() {
		return genre;
	}

	public void setGenre(Genre genre) {
		this.genre = genre;
	}

	public Rating getRating() {
		return rating;
	}

	public void setRating(Rating rating) {
		this.rating = rating;
	}

	public String getDirector() {
		return director;
	}

	public void setDirector(String director) {
		this.director = director;
	}

	public String getCastNames() {
		return castNames;
	}

	public void setCastNames(String castNames) {
		this.castNames = castNames;
	}

	public LocalDate getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(LocalDate releaseDate) {
		this.releaseDate = releaseDate;
	}

	public boolean isNowShowing() {
		return nowShowing;
	}

	public void setNowShowing(boolean nowShowing) {
		this.nowShowing = nowShowing;
	}

	public boolean isFeatured() {
		return featured;
	}

	public void setFeatured(boolean featured) {
		this.featured = featured;
	}

	public String getPosterBg() {
		return posterBg;
	}

	public void setPosterBg(String posterBg) {
		this.posterBg = posterBg;
	}

	public String getPosterAccent() {
		return posterAccent;
	}

	public void setPosterAccent(String posterAccent) {
		this.posterAccent = posterAccent;
	}

	public String getMotif() {
		return motif;
	}

	public void setMotif(String motif) {
		this.motif = motif;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
}
