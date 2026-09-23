package smartdesk.booking.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import smartdesk.booking.dto.cache.DeskCacheData;
import smartdesk.booking.entity.Desk;
import smartdesk.booking.exception.ResourceNotFoundException;
import smartdesk.booking.repository.DeskRepository;

@Service
public class DeskCacheService {

    private final DeskRepository deskRepository;

    public DeskCacheService(DeskRepository deskRepository) {
        this.deskRepository = deskRepository;
    }

    @Cacheable(
            cacheNames = "deskMetadata",
            key = "#deskId"
    )
    public DeskCacheData getDeskMetadata(Long deskId) {

        Desk desk = deskRepository.findById(deskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Desk not found: " + deskId
                        )
                );

        return new DeskCacheData(
                desk.getId(),
                desk.getDeskNumber(),
                desk.getFloor().getId(),
                desk.getXCoordinate(),
                desk.getYCoordinate(),
                desk.getStatus().name()
        );
    }
}