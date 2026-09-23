package smartdesk.booking.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
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

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final DeskRepository deskRepository;
    private final UserRepository userRepository;
    private final TeamQuotaService teamQuotaService;

    public BookingService(
            BookingRepository bookingRepository,
            DeskRepository deskRepository,
            UserRepository userRepository,
            TeamQuotaService teamQuotaService) {

        this.bookingRepository = bookingRepository;
        this.deskRepository = deskRepository;
        this.userRepository = userRepository;
        this.teamQuotaService = teamQuotaService;
    }
    @Transactional
    public BookingResponse createBooking(
            DeskBookingRequest request) {

        validateRequest(request);

        User user = userRepository.findById(
                request.getUserId()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "User not found: "
                                + request.getUserId()
                )
        );

        if (!user.isActive()) {
            throw new IllegalStateException(
                    "User is inactive"
            );
        }

        Desk desk = deskRepository.findByIdForUpdate(
                request.getDeskId()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Desk not found: "
                                + request.getDeskId()
                ));

        if (desk.getStatus() != DeskStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Desk is not active"
            );
        }

        boolean deskAlreadyBooked =
                bookingRepository
                        .existsByDeskAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                                desk,
                                BookingStatus.CONFIRMED,
                                request.getEndTime(),
                                request.getStartTime()
                        );

        if (deskAlreadyBooked) {
            throw new IllegalStateException(
                    "Desk is already booked for the requested time"
            );
        }

        boolean quotaAvailable =
                teamQuotaService.hasQuotaAvailableForUpdate(
                        user.getTeam(),
                        desk.getFloor(),
                        request.getStartTime(),
                        request.getEndTime()
                );

        if (!quotaAvailable) {
            throw new IllegalStateException(
                    "Team booking quota exceeded"
            );
        }

        Booking booking = new Booking(
                user,
                desk,
                request.getStartTime(),
                request.getEndTime()
        );

        Booking savedBooking =
                bookingRepository.save(booking);

        return toResponse(savedBooking);
    }

    private void validateRequest(
            DeskBookingRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Booking request cannot be null"
            );
        }

        if (request.getUserId() == null) {
            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }

        if (request.getDeskId() == null) {
            throw new IllegalArgumentException(
                    "Desk ID is required"
            );
        }

        if (request.getStartTime() == null ||
                request.getEndTime() == null) {

            throw new IllegalArgumentException(
                    "Start time and end time are required"
            );
        }

        if (!request.getStartTime()
                .isBefore(request.getEndTime())) {

            throw new IllegalArgumentException(
                    "Start time must be before end time"
            );
        }
    }

    private BookingResponse toResponse(
            Booking booking) {

        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getDesk().getId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus()
        );
    }
}