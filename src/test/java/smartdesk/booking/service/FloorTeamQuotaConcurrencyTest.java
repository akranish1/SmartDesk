package smartdesk.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import smartdesk.booking.dto.request.DeskBookingRequest;
import smartdesk.booking.dto.response.BookingResponse;
import smartdesk.booking.entity.Booking;
import smartdesk.booking.entity.BookingStatus;
import smartdesk.booking.entity.Desk;
import smartdesk.booking.entity.DeskStatus;
import smartdesk.booking.entity.Floor;
import smartdesk.booking.entity.FloorTeamQuota;
import smartdesk.booking.entity.Team;
import smartdesk.booking.entity.User;
import smartdesk.booking.repository.BookingRepository;
import smartdesk.booking.repository.DeskRepository;
import smartdesk.booking.repository.FloorTeamQuotaRepository;
import smartdesk.booking.repository.UserRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class FloorTeamQuotaConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeskRepository deskRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FloorTeamQuotaRepository floorTeamQuotaRepository;

    @Test
    void shouldAllowOnlyOneBookingWhenTeamQuotaIsOne()
            throws Exception {

        // ---------------------------------------------------------
        // 1. Load users
        // ---------------------------------------------------------

        User alice = userRepository
                .findByEmployeeCode("EMP001")
                .orElseThrow();

        User bob = userRepository
                .findByEmployeeCode("EMP002")
                .orElseThrow();

        // ---------------------------------------------------------
        // 2. Find reference desk and floor
        // ---------------------------------------------------------

        Desk referenceDesk = deskRepository.findAll()
                .stream()
                .filter(d -> "D101".equals(d.getDeskNumber()))
                .findFirst()
                .orElseThrow();

        Floor floor = referenceDesk.getFloor();

        Team team = alice.getTeam();

        // ---------------------------------------------------------
        // 3. Find quota
        // ---------------------------------------------------------

        FloorTeamQuota quota =
                floorTeamQuotaRepository
                        .findByFloorAndTeam(floor, team)
                        .orElseThrow();

        int originalQuota =
                quota.getMaxConcurrentBookings();

        // ---------------------------------------------------------
        // 4. Set quota = 1
        // ---------------------------------------------------------

        quota.setMaxConcurrentBookings(1);
        floorTeamQuotaRepository.saveAndFlush(quota);

        // ---------------------------------------------------------
        // 5. Get at least two active desks
        // ---------------------------------------------------------

        List<Desk> activeDesks =
                deskRepository.findByFloorAndStatus(
                        floor,
                        DeskStatus.ACTIVE
                );

        if (activeDesks.size() < 2) {
            throw new IllegalStateException(
                    "At least two active desks are required"
            );
        }

        /*
         * We deliberately use only two desks.
         *
         * The purpose of this test is NOT desk locking.
         * We want multiple different desks competing
         * for the SAME team/floor quota.
         */
        Desk desk1 = activeDesks.get(0);
        Desk desk2 = activeDesks.get(1);

        // ---------------------------------------------------------
        // 6. Fresh booking interval
        // ---------------------------------------------------------

        Instant startTime =
                Instant.now().plus(7, java.time.temporal.ChronoUnit.DAYS);

        Instant endTime =
                startTime.plus(2, java.time.temporal.ChronoUnit.HOURS);

        // ---------------------------------------------------------
        // 7. Remove existing CONFIRMED bookings
        //    for this exact interval
        // ---------------------------------------------------------

        List<Long> oldBookingIds = new ArrayList<>();

        for (Desk desk : List.of(desk1, desk2)) {

            List<Booking> existingBookings =
                    bookingRepository
                            .findByDeskAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                                    desk,
                                    BookingStatus.CONFIRMED,
                                    endTime,
                                    startTime
                            );

            existingBookings.forEach(
                    booking ->
                            oldBookingIds.add(booking.getId())
            );
        }

        if (!oldBookingIds.isEmpty()) {
            bookingRepository.deleteAllById(oldBookingIds);
            bookingRepository.flush();
        }

        // ---------------------------------------------------------
        // 8. Concurrency setup
        // ---------------------------------------------------------

        int numberOfRequests = 10;

        ExecutorService executor =
                Executors.newFixedThreadPool(numberOfRequests);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Future<Long>> futures =
                new ArrayList<>();

        List<Long> createdBookingIds =
                new ArrayList<>();

        try {

            // -----------------------------------------------------
            // 9. Create concurrent booking requests
            // -----------------------------------------------------

            for (int i = 0; i < numberOfRequests; i++) {

                final User user =
                        (i % 2 == 0)
                                ? alice
                                : bob;

                /*
                 * Alternate between two DIFFERENT desks.
                 *
                 * Request:
                 *
                 * 0 -> desk1
                 * 1 -> desk2
                 * 2 -> desk1
                 * 3 -> desk2
                 * ...
                 *
                 * Therefore the test is specifically
                 * checking team/floor quota concurrency.
                 */

                final Desk desk =
                        (i % 2 == 0)
                                ? desk1
                                : desk2;

                Callable<Long> task = () -> {

                    startLatch.await();

                    BookingResponse response =
                            bookingService.createBooking(
                                    new DeskBookingRequest(
                                            desk.getId(),
                                            startTime,
                                            endTime
                                    ),
                                    user.getId()
                            );

                    return response.getBookingId();
                };

                futures.add(
                        executor.submit(task)
                );
            }

            // -----------------------------------------------------
            // 10. Release all requests together
            // -----------------------------------------------------

            System.out.println(
                    "Starting " + numberOfRequests +
                            " concurrent booking requests..."
            );

            startLatch.countDown();

            // -----------------------------------------------------
            // 11. Collect results
            // -----------------------------------------------------

            int successfulBookings = 0;
            int rejectedBookings = 0;

            System.out.println(
                    "Expected requests = " +
                            numberOfRequests
            );

            System.out.println(
                    "Submitted futures = " +
                            futures.size()
            );

            assertEquals(
                    numberOfRequests,
                    futures.size()
            );

            for (Future<Long> future : futures) {

                try {

                    Long bookingId = future.get();

                    successfulBookings++;

                    createdBookingIds.add(bookingId);

                    System.out.println(
                            "SUCCESS -> bookingId=" +
                                    bookingId
                    );

                } catch (ExecutionException e) {

                    rejectedBookings++;

                    Throwable cause = e.getCause();

                    System.out.println(
                            "REJECTED -> " +
                                    (
                                            cause != null
                                                    ? cause.getClass()
                                                    .getSimpleName()
                                                    + " : "
                                                    + cause.getMessage()
                                                    : e.getMessage()
                                    )
                    );

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                    throw new AssertionError(
                            "Test thread was interrupted",
                            e
                    );
                }
            }

            // -----------------------------------------------------
            // 12. Print final result
            // -----------------------------------------------------

            System.out.println("--------------------------------");
            System.out.println(
                    "TOTAL SUCCESS = " +
                            successfulBookings
            );

            System.out.println(
                    "TOTAL REJECTED = " +
                            rejectedBookings
            );
            System.out.println("--------------------------------");

            // -----------------------------------------------------
            // 13. Main concurrency assertion
            // -----------------------------------------------------

            assertEquals(
                    1,
                    successfulBookings,
                    "Exactly one booking should succeed because " +
                            "team/floor quota is 1"
            );

            assertEquals(
                    numberOfRequests - 1,
                    rejectedBookings,
                    "All remaining requests should be rejected"
            );

            // -----------------------------------------------------
            // 14. Verify database state
            // -----------------------------------------------------

            long totalConfirmedBookings = 0;

            for (Desk desk : List.of(desk1, desk2)) {

                List<Booking> bookings =
                        bookingRepository
                                .findByDeskAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                                        desk,
                                        BookingStatus.CONFIRMED,
                                        endTime,
                                        startTime
                                );

                totalConfirmedBookings += bookings.size();
            }

            assertEquals(
                    1,
                    totalConfirmedBookings,
                    "Database should contain exactly one " +
                            "confirmed booking"
            );

        } finally {

            // -----------------------------------------------------
            // 15. Cleanup created bookings
            // -----------------------------------------------------

            if (!createdBookingIds.isEmpty()) {

                bookingRepository.deleteAllById(
                        createdBookingIds
                );

                bookingRepository.flush();
            }

            // -----------------------------------------------------
            // 16. Restore original quota
            // -----------------------------------------------------

            quota.setMaxConcurrentBookings(
                    originalQuota
            );

            floorTeamQuotaRepository.saveAndFlush(quota);

            // -----------------------------------------------------
            // 17. Shutdown executor
            // -----------------------------------------------------

            executor.shutdown();

            if (!executor.awaitTermination(
                    10,
                    TimeUnit.SECONDS
            )) {

                executor.shutdownNow();
            }
        }
    }
}