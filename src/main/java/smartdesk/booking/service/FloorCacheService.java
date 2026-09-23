package smartdesk.booking.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import smartdesk.booking.dto.cache.FloorCacheData;
import smartdesk.booking.entity.Floor;
import smartdesk.booking.exception.ResourceNotFoundException;
import smartdesk.booking.repository.FloorRepository;

@Service
public class FloorCacheService {

    private final FloorRepository floorRepository;

    public FloorCacheService(FloorRepository floorRepository) {
        this.floorRepository = floorRepository;
    }

    @Cacheable(cacheNames = "floorMetadata", key = "#floorId")
    public FloorCacheData getFloorMetadata(Long floorId) {

        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Floor not found: " + floorId
                        )
                );

        return new FloorCacheData(
                floor.getId(),
                floor.getBuilding(),
                floor.getFloorNumber(),
                floor.getName(),
                floor.getTimezone()
        );
    }
}