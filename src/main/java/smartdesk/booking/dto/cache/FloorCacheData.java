package smartdesk.booking.dto.cache;

public record FloorCacheData(
        Long id,
        String building,
        Integer floorNumber,
        String name,
        String timezone
) {
}