package smartdesk.booking.dto.response;

import smartdesk.booking.entity.BookingStatus;

import java.time.Instant;

public class BookingResponse {

    private Long bookingId;
    private Long userId;
    private Long deskId;
    private Instant startTime;
    private Instant endTime;
    private BookingStatus status;

    public BookingResponse(
            Long bookingId,
            Long userId,
            Long deskId,
            Instant startTime,
            Instant endTime,
            BookingStatus status) {

        this.bookingId = bookingId;
        this.userId = userId;
        this.deskId = deskId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getDeskId() {
        return deskId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public BookingStatus getStatus() {
        return status;
    }
}