package smartdesk.booking.dto.request;

import java.time.Instant;

public class DeskPlacementRequest {

    private Long floorId;
    private Instant startTime;
    private Instant endTime;
    private Long preferredDeskId;

    public DeskPlacementRequest() {
    }

    public DeskPlacementRequest(
            Long floorId,
            Instant startTime,
            Instant endTime,
            Long preferredDeskId) {

        this.floorId = floorId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.preferredDeskId = preferredDeskId;
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

    public Long getPreferredDeskId() {
        return preferredDeskId;
    }

    public void setPreferredDeskId(Long preferredDeskId) {
        this.preferredDeskId = preferredDeskId;
    }
}