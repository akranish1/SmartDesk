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
        // 2. Find Engineering floor
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
        // 4. Use quota = 1 for this test
        // ---------------------------------------------------------

        quota.setMaxConcurrentBookings(1);

        floorTeamQuotaRepository.saveAndFlush(quota);


        // ---------------------------------------------------------
        // 5. Get active desks
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


        // ---------------------------------------------------------
        // 6. Completely fresh booking interval
        // ---------------------------------------------------------

        Instant startTime =
                Instant.parse("2099-10-02T10:00:00Z");

        Instant endTime =
                Instant.parse("2099-10-02T12:00:00Z");


        // ---------------------------------------------------------
        // 7. Make sure this interval is clean
        // ---------------------------------------------------------

        List<Long> oldBookingIds = new ArrayList<>();

        for (Desk desk : activeDesks) {

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
                            oldBookingIds.add(
                                    booking.getId()
                            )
            );
        }

        bookingRepository.deleteAllById(
                oldBookingIds
        );


        // ---------------------------------------------------------
        // 8. Concurrency setup
        // ---------------------------------------------------------

        int numberOfRequests = 10;

        ExecutorService executor =
                Executors.newFixedThreadPool(
                        numberOfRequests
                );

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Future<Long>> futures =
                new ArrayList<>();

        List<Long> createdBookingIds =
                new ArrayList<>();


        try {

            // -----------------------------------------------------
            // 9. Create 10 concurrent requests
            // -----------------------------------------------------

            for (int i = 0;
                 i < numberOfRequests;
                 i++) {

                final User user =
                        (i % 2 == 0)
                                ? alice
                                : bob;

                /*
                 * Different desks.
                 *
                 * 0 -> D101
                 * 1 -> D102
                 * 2 -> D104
                 * 3 -> D101
                 * ...
                 *
                 * Therefore desk locking is NOT the
                 * resource being tested.
                 */

                final Desk desk =
                        activeDesks.get(
                                i % activeDesks.size()
                        );

                Callable<Long> task = () -> {

                    startLatch.await();

                    BookingResponse response =
                            bookingService.createBooking(
                                    new DeskBookingRequest(

                                            desk.getId(),
                                            startTime,
                                            endTime
                                    ), user.getId()
                            );

                    return response.getBookingId();
                };

                futures.add(
                        executor.submit(task)
                );
            }


            // -----------------------------------------------------
            // 10. Start all requests together
            // -----------------------------------------------------

            startLatch.countDown();


            // -----------------------------------------------------
            // 11. Collect results
            // -----------------------------------------------------

            int successfulBookings = 0;
            int rejectedBookings = 0;

            System.out.println(
                    "Expected requests = " + numberOfRequests
            );

            System.out.println(
                    "Submitted futures = " + futures.size()
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
                            "SUCCESS -> bookingId="
                                    + bookingId
                    );

                } catch (ExecutionException e) {

                    rejectedBookings++;

                    Throwable cause = e.getCause();

                    System.out.println(
                            "REJECTED -> "
                                    + (
                                    cause != null
                                            ? cause.getClass().getSimpleName()
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

            System.out.println(
                    "TOTAL SUCCESS = "
                            + successfulBookings
            );

            System.out.println(
                    "TOTAL REJECTED = "
                            + rejectedBookings
            );

            assertEquals(
                    1,
                    successfulBookings
            );

            assertEquals(
                    numberOfRequests - successfulBookings,
                    rejectedBookings
            );


            // -----------------------------------------------------
            // 12. Verify application result
            // -----------------------------------------------------

            System.out.println(
                    "TOTAL SUCCESS = "
                            + successfulBookings
            );

            System.out.println(
                    "TOTAL REJECTED = "
                            + rejectedBookings
            );

            assertEquals(
                    1,
                    successfulBookings
            );

            assertEquals(
                    9,
                    rejectedBookings
            );


            // -----------------------------------------------------
            // 13. Verify database state
            // -----------------------------------------------------

            long totalConfirmedBookings = 0;

            for (Desk desk : activeDesks) {

                List<Booking> bookings =
                        bookingRepository
                                .findByDeskAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                                        desk,
                                        BookingStatus.CONFIRMED,
                                        endTime,
                                        startTime
                                );

                totalConfirmedBookings +=
                        bookings.size();
            }

            assertEquals(
                    1,
                    totalConfirmedBookings
            );

        } finally {

            // -----------------------------------------------------
            // 14. Delete test bookings
            // -----------------------------------------------------

            bookingRepository.deleteAllById(
                    createdBookingIds
            );


            // -----------------------------------------------------
            // 15. Restore original quota
            // -----------------------------------------------------

            quota.setMaxConcurrentBookings(
                    originalQuota
            );

            floorTeamQuotaRepository.saveAndFlush(
                    quota
            );


            // -----------------------------------------------------
            // 16. Shutdown executor
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