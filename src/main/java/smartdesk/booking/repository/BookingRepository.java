package smartdesk.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smartdesk.booking.entity.Booking;
import smartdesk.booking.entity.BookingStatus;
import smartdesk.booking.entity.Desk;

import java.time.Instant;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByDeskAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            Desk desk,
            BookingStatus status,
            Instant endTime,
            Instant startTime
    );

    boolean existsByDeskAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            Desk desk,
            BookingStatus status,
            Instant endTime,
            Instant startTime
    );
}