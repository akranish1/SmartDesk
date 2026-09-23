package smartdesk.booking.service;

import org.springframework.stereotype.Service;
import smartdesk.booking.dto.request.DeskSearchRequest;
import smartdesk.booking.dto.response.DeskResponse;
import smartdesk.booking.entity.Desk;
import smartdesk.booking.entity.DeskStatus;
import smartdesk.booking.entity.Floor;
import smartdesk.booking.exception.InvalidBookingException;
import smartdesk.booking.exception.ResourceNotFoundException;
import smartdesk.booking.repository.DeskRepository;
import smartdesk.booking.repository.FloorRepository;

import java.util.List;

@Service
public class DeskSearchService {

    private final DeskRepository deskRepository;
    private final FloorRepository floorRepository;

    public DeskSearchService(
            DeskRepository deskRepository,
            FloorRepository floorRepository) {

        this.deskRepository = deskRepository;
        this.floorRepository = floorRepository;
    }
    public List<Desk> findAvailableDeskEntities(
            Long floorId,
            java.time.Instant startTime,
            java.time.Instant endTime) {

        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Floor not found: " + floorId
                        )
                );

        return deskRepository.findAvailableDesks(
                floor,
                DeskStatus.ACTIVE,
                startTime,
                endTime
        );
    }

    public List<DeskResponse> searchAvailableDesks(
            DeskSearchRequest request) {

        validateRequest(request);

        Floor floor = floorRepository.findById(request.getFloorId())

                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Floor not found: " + request.getFloorId()
                                )
                        );

        List<Desk> desks = deskRepository.findAvailableDesks(
                floor,
                DeskStatus.ACTIVE,
                request.getStartTime(),
                request.getEndTime()
        );

        return desks.stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateRequest(
            DeskSearchRequest request) {

        if (request == null) {
            throw new InvalidBookingException(
                    "Search request cannot be null"
            );
        }

        if (request.getFloorId() == null) {
            throw new InvalidBookingException(
                    "Floor ID is required"
            );
        }

        if (request.getStartTime() == null ||
                request.getEndTime() == null) {

            throw new InvalidBookingException(
                    "Start time and end time are required"
            );
        }

        if (!request.getStartTime()
                .isBefore(request.getEndTime())) {

            throw new InvalidBookingException(
                    "Start time must be before end time"
            );
        }
    }

    private DeskResponse toResponse(Desk desk) {

        return new DeskResponse(
                desk.getId(),
                desk.getFloor().getId(),
                desk.getStatus()
        );
    }
}