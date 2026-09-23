package smartdesk.booking.dto.cache;

public record DeskCacheData(
        Long id,
        String deskNumber,
        Long floorId,
        int xCoordinate,
        int yCoordinate,
        String status
) {
}