package smartdesk.booking.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class DeskBookingRequest {

    @NotNull(message = "Desk ID is required")
    private Long deskId;

    @NotNull(message = "Start time is required")
    private Instant startTime;

    @NotNull(message = "End time is required")
    private Instant endTime;

    public DeskBookingRequest() {}

    public DeskBookingRequest(
            Long deskId,
            Instant startTime,
            Instant endTime
    ) {
        this.deskId = deskId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getDeskId() {
        return deskId;
    }

    public void setDeskId(Long deskId) {
        this.deskId = deskId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }
}