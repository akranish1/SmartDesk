package smartdesk.booking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartdesk.booking.dto.request.DeskBookingRequest;
import smartdesk.booking.dto.response.BookingResponse;
import smartdesk.booking.entity.Booking;
import smartdesk.booking.entity.BookingStatus;
import smartdesk.booking.entity.Desk;
import smartdesk.booking.entity.DeskStatus;
import smartdesk.booking.entity.User;
import smartdesk.booking.exception.BookingConflictException;
import smartdesk.booking.exception.DeskUnavailableException;
import smartdesk.booking.exception.InvalidBookingException;
import smartdesk.booking.exception.QuotaExceededException;
import smartdesk.booking.exception.ResourceNotFoundException;
import smartdesk.booking.repository.BookingRepository;
import smartdesk.booking.repository.DeskRepository;
import smartdesk.booking.repository.UserRepository;

import java.time.Instant;

@Service
public class BookingService {

    private final BookingWindowService bookingWindowService;
    private final BookingRepository bookingRepository;
    private final DeskRepository deskRepository;
    private final UserRepository userRepository;
    private final TeamQuotaService teamQuotaService;

    public BookingService(
            BookingRepository bookingRepository,
            DeskRepository deskRepository,
            UserRepository userRepository,
            TeamQuotaService teamQuotaService,
            BookingWindowService bookingWindowService) {

        this.bookingRepository = bookingRepository;
        this.deskRepository = deskRepository;
        this.userRepository = userRepository;
        this.teamQuotaService = teamQuotaService;
        this.bookingWindowService = bookingWindowService;
    }

    @Transactional
    public BookingResponse createBooking(
            DeskBookingRequest request,
            Long userId) {

        validateRequest(request, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + userId
                        )
                );

        if (!user.isActive()) {
            throw new InvalidBookingException(
                    "User is inactive"
            );
        }

        Desk desk = deskRepository.findByIdForUpdate(
                request.getDeskId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Desk not found: " + request.getDeskId()
                )
        );
        bookingWindowService.validate(
                request.getStartTime(),
                request.getEndTime(),
                desk.getFloor().getTimezone()
        );
        if (desk.getStatus() != DeskStatus.ACTIVE) {
            throw new DeskUnavailableException(
                    "Desk is not active: " + desk.getId()
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
            throw new BookingConflictException(
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
            throw new QuotaExceededException(
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

    @Transactional
    public BookingResponse checkIn(
            Long bookingId,
            Long userId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking not found: " + bookingId
                        )
                );

        // User can only check in to their own booking
        if (!booking.getUser().getId().equals(userId)) {
            throw new InvalidBookingException(
                    "You can only check in to your own booking"
            );
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingException(
                    "Only confirmed bookings can be checked in"
            );
        }

        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setCheckedInAt(Instant.now());

        Booking savedBooking = bookingRepository.save(booking);

        return toResponse(savedBooking);
    }
    private void validateRequest(
            DeskBookingRequest request,
            Long userId) {

        if (request == null) {
            throw new InvalidBookingException(
                    "Booking request must not be null"
            );
        }

        if (userId == null) {
            throw new InvalidBookingException(
                    "Authenticated user ID must not be null"
            );
        }

        if (request.getDeskId() == null) {
            throw new InvalidBookingException(
                    "Desk ID must not be null"
            );
        }

        if (request.getStartTime() == null) {
            throw new InvalidBookingException(
                    "Start time must not be null"
            );
        }

        if (request.getEndTime() == null) {
            throw new InvalidBookingException(
                    "End time must not be null"
            );
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new InvalidBookingException(
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