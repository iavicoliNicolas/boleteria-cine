package ar.com.platea.repo;

import ar.com.platea.domain.Cinema;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {

	@EntityGraph(attributePaths = "auditoriums")
	List<Cinema> findAllByOrderBySortOrderAsc();

	@EntityGraph(attributePaths = "auditoriums")
	Optional<Cinema> findBySlug(String slug);
}
