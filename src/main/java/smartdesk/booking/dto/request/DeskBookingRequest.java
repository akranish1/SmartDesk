package smartdesk.booking.dto.request;

import java.time.Instant;

public class DeskBookingRequest {

    private Long deskId;
    private Instant startTime;
    private Instant endTime;

    public DeskBookingRequest() {
    }

    public DeskBookingRequest(
            Long deskId,
            Instant startTime,
            Instant endTime) {

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