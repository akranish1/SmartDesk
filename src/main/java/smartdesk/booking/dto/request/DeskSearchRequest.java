package smartdesk.booking.dto.request;

import java.time.Instant;

public class DeskSearchRequest {

    private Long floorId;
    private Instant startTime;
    private Instant endTime;

    public DeskSearchRequest() {
    }

    public DeskSearchRequest(
            Long floorId,
            Instant startTime,
            Instant endTime) {
        this.floorId = floorId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getFloorId() {
        return floorId;
    }

    public void setFloorId(Long floorId) {
        this.floorId = floorId;
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