package smartdesk.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import smartdesk.booking.dto.request.DeskBookingRequest;
import smartdesk.booking.dto.response.BookingResponse;
import smartdesk.booking.entity.Booking;
import smartdesk.booking.entity.BookingStatus;
import smartdesk.booking.entity.Desk;
import smartdesk.booking.entity.DeskStatus;
import smartdesk.booking.entity.User;
import smartdesk.booking.repository.BookingRepository;
import smartdesk.booking.repository.DeskRepository;
import smartdesk.booking.repository.UserRepository;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class NoShowServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private NoShowService noShowService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeskRepository deskRepository;

    @Test
    void shouldMarkBookingAsNoShow() {

        User user = userRepository
                .findByEmployeeCode("EMP001")
                .orElseThrow();

        Desk desk = deskRepository.findAll()
                .stream()
                .filter(d -> d.getStatus() == DeskStatus.ACTIVE)
                .findFirst()
                .orElseThrow();

        /*
         * The booking must be created through BookingService,
         * so we still test the real booking flow.
         */
        Instant startTime =
                Instant.now().minusSeconds(20 * 60);

        Instant endTime =
                startTime.plusSeconds(8 * 60 * 60);

        /*
         * Our current BookingWindowService will reject a
         * booking in the past.
         *
         * Therefore, for this test we should create the
         * Booking entity directly.
         */

        Booking booking = new Booking(
                user,
                desk,
                startTime,
                endTime
        );

        booking.setStatus(BookingStatus.CONFIRMED);

        Booking savedBooking =
                bookingRepository.saveAndFlush(booking);

        Long bookingId = savedBooking.getId();

        // Execute no-show detection
        noShowService.releaseNoShows();

        // Read the booking again
        Booking updatedBooking =
                bookingRepository.findById(bookingId)
                        .orElseThrow();

        assertEquals(
                BookingStatus.NO_SHOW,
                updatedBooking.getStatus()
        );

        assertNull(
                updatedBooking.getCheckedInAt()
        );
    }

    @Test
    void shouldNotMarkCheckedInBookingAsNoShow() {

        User user = userRepository
                .findByEmployeeCode("EMP001")
                .orElseThrow();

        Desk desk = deskRepository.findAll()
                .stream()
                .filter(d -> d.getStatus() == DeskStatus.ACTIVE)
                .findFirst()
                .orElseThrow();

        Instant startTime =
                Instant.now().minusSeconds(20 * 60);

        Instant endTime =
                startTime.plusSeconds(8 * 60 * 60);

        Booking booking = new Booking(
                user,
                desk,
                startTime,
                endTime
        );

        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setCheckedInAt(Instant.now().minusSeconds(10 * 60));

        Booking savedBooking =
                bookingRepository.saveAndFlush(booking);

        noShowService.releaseNoShows();

        Booking updatedBooking =
                bookingRepository.findById(savedBooking.getId())
                        .orElseThrow();

        assertEquals(
                BookingStatus.CHECKED_IN,
                updatedBooking.getStatus()
        );

        assertNotNull(
                updatedBooking.getCheckedInAt()
        );
    }


}