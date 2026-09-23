package smartdesk.booking.service;

import org.springframework.stereotype.Service;
import smartdesk.booking.dto.request.DeskPlacementRequest;
import smartdesk.booking.dto.response.DeskPlacementResponse;
import smartdesk.booking.entity.Desk;
import smartdesk.booking.exception.DeskUnavailableException;
import smartdesk.booking.exception.InvalidBookingException;
import smartdesk.booking.exception.ResourceNotFoundException;
import smartdesk.booking.repository.DeskRepository;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

@Service
public class DeskPlacementService {

    private final DeskRepository deskRepository;
    private final DeskSearchService deskSearchService;

    public DeskPlacementService(
            DeskRepository deskRepository,
            DeskSearchService deskSearchService) {

        this.deskRepository = deskRepository;
        this.deskSearchService = deskSearchService;
    }

    public DeskPlacementResponse findNearestDesk(
            DeskPlacementRequest request) {

        validateRequest(request);

        // Find preferred desk
        Desk preferredDesk =
                deskRepository.findById(request.getPreferredDeskId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Preferred desk not found: "
                                                + request.getPreferredDeskId()
                                )
                        );

        // Verify preferred desk belongs to requested floor
        if (!preferredDesk.getFloor().getId()
                .equals(request.getFloorId())) {

            throw new InvalidBookingException(
                    "Preferred desk does not belong to the requested floor"
            );
        }

        // Find desks available for the requested time
        List<Desk> availableDesks =
                deskSearchService.findAvailableDeskEntities(
                        request.getFloorId(),
                        request.getStartTime(),
                        request.getEndTime()
                );

        if (availableDesks.isEmpty()) {
            throw new DeskUnavailableException(
                    "No suitable desk is available for the requested time"
            );
        }

        /*
         * PriorityQueue keeps the desk with the smallest
         * Manhattan distance at the head.
         *
         * If two desks have the same distance,
         * desk ID is used as the deterministic tie-breaker.
         */
        PriorityQueue<DeskDistance> queue =
                new PriorityQueue<>(
                        Comparator
                                .comparingInt(DeskDistance::distance)
                                .thenComparingLong(
                                        candidate ->
                                                candidate.desk().getId()
                                )
                );

        for (Desk desk : availableDesks) {

            int distance = calculateManhattanDistance(
                    preferredDesk,
                    desk
            );

            queue.offer(
                    new DeskDistance(desk, distance)
            );
        }

        DeskDistance bestCandidate = queue.poll();

        return new DeskPlacementResponse(
                bestCandidate.desk().getId(),
                bestCandidate.desk().getDeskNumber(),
                bestCandidate.distance()
        );
    }

    private int calculateManhattanDistance(
            Desk first,
            Desk second) {

        return Math.abs(
                first.getXCoordinate()
                        - second.getXCoordinate()
        ) + Math.abs(
                first.getYCoordinate()
                        - second.getYCoordinate()
        );
    }

    private void validateRequest(
            DeskPlacementRequest request) {

        if (request == null) {
            throw new InvalidBookingException(
                    "Placement request cannot be null"
            );
        }

        if (request.getFloorId() == null) {
            throw new InvalidBookingException(
                    "Floor ID is required"
            );
        }

        if (request.getPreferredDeskId() == null) {
            throw new InvalidBookingException(
                    "Preferred desk ID is required"
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

    private record DeskDistance(
            Desk desk,
            int distance) {
    }
}