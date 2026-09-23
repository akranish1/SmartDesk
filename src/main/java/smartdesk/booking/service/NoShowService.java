package smartdesk.booking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartdesk.booking.entity.Booking;
import smartdesk.booking.entity.BookingStatus;
import smartdesk.booking.repository.BookingRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class NoShowService {

    private static final Duration GRACE_PERIOD =
            Duration.ofMinutes(15);

    private final BookingRepository bookingRepository;

    public NoShowService(
            BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public void releaseNoShows() {

        Instant cutoff =
                Instant.now().minus(GRACE_PERIOD);

        List<Booking> bookings =
                bookingRepository.findBookingsEligibleForNoShow(
                        BookingStatus.CONFIRMED,
                        cutoff
                );

        for (Booking booking : bookings) {

            if (booking.getStatus() != BookingStatus.CONFIRMED) {
                continue;
            }

            if (booking.getCheckedInAt() != null) {
                continue;
            }

            booking.setStatus(BookingStatus.NO_SHOW);
        }
    }
}