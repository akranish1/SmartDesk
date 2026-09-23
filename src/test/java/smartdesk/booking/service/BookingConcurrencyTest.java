package smartdesk.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class BookingConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeskRepository deskRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void shouldAllowOnlyOneBookingWhenMultipleRequestsAreMadeConcurrently() {

        User alice = userRepository
                .findByEmployeeCode("EMP001")
                .orElseThrow();

        User bob = userRepository
                .findByEmployeeCode("EMP002")
                .orElseThrow();

        Desk desk = deskRepository.findAll()
                .stream()
                .filter(d -> "D104".equals(d.getDeskNumber()))
                .findFirst()
                .orElseThrow();

        Instant startTime =
                Instant.parse("2026-10-01T10:00:00Z");

        Instant endTime =
                Instant.parse("2026-10-01T12:00:00Z");

        int numberOfRequests = 10;

        ExecutorService executor =
                Executors.newFixedThreadPool(numberOfRequests);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Future<Booking>> futures =
                new ArrayList<>();

        List<Long> createdBookingIds =
                new ArrayList<>();

        for (int i = 0; i < numberOfRequests; i++) {

            final Long userId =
                    (i % 2 == 0)
                            ? alice.getId()
                            : bob.getId();

            Callable<Booking> task = () -> {

                startLatch.await();

                BookingResponse response =
                        bookingService.createBooking(
                                new DeskBookingRequest(
                                        desk.getId(),
                                        startTime,
                                        endTime
                                ),userId
                        );

                return bookingRepository
                        .findById(response.getBookingId())
                        .orElseThrow();
            };

            futures.add(executor.submit(task));
        }

        startLatch.countDown();

        int successfulBookings = 0;
        int rejectedBookings = 0;

        for (Future<Booking> future : futures) {

            try {

                Booking booking = future.get();

                successfulBookings++;

                createdBookingIds.add(booking.getId());

                System.out.println(
                        "Booking succeeded: "
                                + booking.getId()
                );

            } catch (Exception e) {

                rejectedBookings++;

                System.out.println(
                        "Booking rejected: "
                                + e.getCause().getMessage()
                );
            }
        }

        assertEquals(1, successfulBookings);
        assertEquals(9, rejectedBookings);

        List<Booking> confirmedBookings =
                bookingRepository
                        .findByDeskAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                                desk,
                                BookingStatus.CONFIRMED,
                                endTime,
                                startTime
                        );

        assertEquals(1, confirmedBookings.size());

        bookingRepository.deleteAllById(createdBookingIds);

        executor.shutdown();
    }
}