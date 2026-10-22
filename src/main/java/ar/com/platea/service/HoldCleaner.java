package ar.com.platea.service;

import ar.com.platea.domain.SeatStatus;
import ar.com.platea.repo.SeatReservationRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HoldCleaner {

	private final SeatReservationRepository seats;
	private final Clock clock;

	public HoldCleaner(SeatReservationRepository seats, Clock clock) {
		this.seats = seats;
		this.clock = clock;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void releaseExpired() {
		seats.releaseExpired(SeatStatus.HELD, LocalDateTime.now(clock));
	}
}
