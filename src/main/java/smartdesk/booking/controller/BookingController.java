package smartdesk.booking.controller;

import org.springframework.web.bind.annotation.*;
import smartdesk.booking.dto.request.DeskBookingRequest;
import smartdesk.booking.dto.response.BookingResponse;
import smartdesk.booking.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(
            BookingService bookingService) {

        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponse createBooking(
            @RequestBody DeskBookingRequest request) {

        return bookingService.createBooking(request);
    }
}