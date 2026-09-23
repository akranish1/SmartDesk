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
import smartdesk.booking.entity.User;
import smartdesk.booking.repository.BookingRepository;
import smartdesk.booking.repository.DeskRepository;
import smartdesk.booking.repository.UserRepository;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookingCheckInTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeskRepository deskRepository;

    @Test
    void shouldCheckInUserForOwnBooking() {

        // 1. Get existing test user
        User user = userRepository
                .findByEmployeeCode("EMP001")
                .orElseThrow();

        // 2. Get an active desk
        Desk desk = deskRepository.findAll()
                .stream()
                .filter(d -> d.getStatus().name().equals("ACTIVE"))
                .findFirst()
                .orElseThrow();

        // 3. Create a booking in the valid booking window
        Instant startTime =
                Instant.now().plusSeconds(60 * 60);

        Instant endTime =
                startTime.plusSeconds(60 * 60 * 8);

        DeskBookingRequest request =
                new DeskBookingRequest(
                        desk.getId(),
                        startTime,
                        endTime
                );

        BookingResponse createdBooking =
                bookingService.createBooking(
                        request,
                        user.getId()
                );

        Long bookingId = createdBooking.getBookingId();

        // 4. Verify booking was created as CONFIRMED
        Booking booking =
                bookingRepository.findById(bookingId)
                        .orElseThrow();

        assertEquals(
                BookingStatus.CONFIRMED,
                booking.getStatus()
        );

        assertNull(booking.getCheckedInAt());

        // 5. Check in
        BookingResponse checkInResponse =
                bookingService.checkIn(
                        bookingId,
                        user.getId()
                );

        // 6. Verify response
        assertEquals(
                BookingStatus.CHECKED_IN,
                checkInResponse.getStatus()
        );

        // 7. Verify database state
        Booking updatedBooking =
                bookingRepository.findById(bookingId)
                        .orElseThrow();

        assertEquals(
                BookingStatus.CHECKED_IN,
                updatedBooking.getStatus()
        );

        assertNotNull(
                updatedBooking.getCheckedInAt()
        );

        // No manual delete required.
        // @Transactional rolls the test back after completion.
    }
}