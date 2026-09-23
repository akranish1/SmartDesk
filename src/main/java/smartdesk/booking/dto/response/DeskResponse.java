package smartdesk.booking.dto.response;

import smartdesk.booking.entity.DeskStatus;

public class DeskResponse {

    private Long id;
    private Long floorId;
    private DeskStatus status;

    public DeskResponse() {
    }

    public DeskResponse(
            Long id,
            Long floorId,
            DeskStatus status) {

        this.id = id;
        this.floorId = floorId;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getFloorId() {
        return floorId;
    }

    public DeskStatus getStatus() {
        return status;
    }
}