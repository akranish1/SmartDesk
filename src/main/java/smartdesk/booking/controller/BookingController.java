package smartdesk.booking.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import smartdesk.booking.dto.request.DeskBookingRequest;
import smartdesk.booking.dto.response.BookingResponse;
import smartdesk.booking.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponse createBooking(
            @RequestBody DeskBookingRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return bookingService.createBooking(request, userId);
    }
}