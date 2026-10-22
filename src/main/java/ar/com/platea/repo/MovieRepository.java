package ar.com.platea.repo;

import ar.com.platea.domain.Movie;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {

	Optional<Movie> findBySlug(String slug);

	List<Movie> findAllByOrderBySortOrderAsc();
}
