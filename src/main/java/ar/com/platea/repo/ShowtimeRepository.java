package ar.com.platea.repo;

import ar.com.platea.domain.Movie;
import ar.com.platea.domain.Showtime;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from Showtime s where s.id = :id")
	Optional<Showtime> lockById(@Param("id") Long id);

	@Query("""
			select s from Showtime s
			join fetch s.movie m
			join fetch s.auditorium a
			join fetch a.cinema c
			where s.startsAt >= :from and s.startsAt < :to
			order by s.startsAt
			""")
	List<Showtime> findBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("""
			select s from Showtime s
			join fetch s.movie m
			join fetch s.auditorium a
			join fetch a.cinema c
			where s.startsAt >= :from and s.startsAt < :to and c.city = :city
			order by s.startsAt
			""")
	List<Showtime> findBetweenInCity(
			@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("city") String city);

	@Query("""
			select s from Showtime s
			join fetch s.auditorium a
			join fetch a.cinema c
			where s.movie = :movie and s.startsAt >= :from and s.startsAt < :to
			order by c.sortOrder asc, s.startsAt asc
			""")
	List<Showtime> findForMovie(
			@Param("movie") Movie movie, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("""
			select s from Showtime s
			join fetch s.movie m
			join fetch s.auditorium a
			join fetch a.cinema c
			where c.id = :cinemaId and s.startsAt >= :from and s.startsAt < :to
			order by s.startsAt
			""")
	List<Showtime> findForCinema(
			@Param("cinemaId") Long cinemaId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("""
			select s from Showtime s
			join fetch s.movie m
			join fetch s.auditorium a
			join fetch a.cinema c
			where s.id = :id
			""")
	Optional<Showtime> findDetailed(@Param("id") Long id);
}
