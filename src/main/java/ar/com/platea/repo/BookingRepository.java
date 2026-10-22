package ar.com.platea.repo;

import ar.com.platea.domain.Booking;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

	boolean existsByCode(String code);

	@EntityGraph(attributePaths = {"showtime", "showtime.movie", "showtime.auditorium", "showtime.auditorium.cinema", "seats"})
	Optional<Booking> findByCode(String code);

	@EntityGraph(attributePaths = {"showtime", "showtime.movie", "showtime.auditorium", "showtime.auditorium.cinema", "seats"})
	List<Booking> findByEmailOrderByCreatedAtDesc(String email);
}
