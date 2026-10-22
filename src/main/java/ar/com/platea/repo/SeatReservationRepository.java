package ar.com.platea.repo;

import ar.com.platea.domain.SeatReservation;
import ar.com.platea.domain.SeatStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {

	List<SeatReservation> findByShowtimeId(Long showtimeId);

	List<SeatReservation> findByShowtimeIdAndSeatCodeIn(Long showtimeId, Collection<String> codes);

	@Query("""
			select r from SeatReservation r
			join fetch r.showtime s
			join fetch s.movie
			join fetch s.auditorium a
			join fetch a.cinema
			where r.sessionId = :sessionId and r.status = :status
			order by r.rowLabel asc, r.seatNumber asc
			""")
	List<SeatReservation> findHolds(@Param("sessionId") String sessionId, @Param("status") SeatStatus status);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("delete from SeatReservation r where r.status = :status and r.expiresAt <= :now")
	int releaseExpired(@Param("status") SeatStatus status, @Param("now") LocalDateTime now);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("delete from SeatReservation r where r.sessionId = :sessionId and r.status = :status")
	int releaseSession(@Param("sessionId") String sessionId, @Param("status") SeatStatus status);

	@Query("""
			select r.showtime.id, count(r) from SeatReservation r
			where r.showtime.id in :ids
			group by r.showtime.id
			""")
	List<Object[]> countByShowtimeIds(@Param("ids") Collection<Long> ids);
}
