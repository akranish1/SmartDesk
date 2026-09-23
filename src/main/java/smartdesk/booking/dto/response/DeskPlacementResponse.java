package smartdesk.booking.dto.response;

public class DeskPlacementResponse {

    private Long deskId;
    private String deskNumber;
    private int distance;

    public DeskPlacementResponse(
            Long deskId,
            String deskNumber,
            int distance) {

        this.deskId = deskId;
        this.deskNumber = deskNumber;
        this.distance = distance;
    }

    public Long getDeskId() {
        return deskId;
    }

    public String getDeskNumber() {
        return deskNumber;
    }

    public int getDistance() {
        return distance;
    }
}