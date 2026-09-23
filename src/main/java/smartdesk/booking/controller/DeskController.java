package smartdesk.booking.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import smartdesk.booking.dto.request.DeskPlacementRequest;
import smartdesk.booking.dto.request.DeskSearchRequest;
import smartdesk.booking.dto.response.DeskPlacementResponse;
import smartdesk.booking.dto.response.DeskResponse;
import smartdesk.booking.service.DeskPlacementService;
import smartdesk.booking.service.DeskSearchService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/desks")
public class DeskController {

    private final DeskSearchService deskSearchService;
    private final DeskPlacementService deskPlacementService;

    public DeskController(
            DeskSearchService deskSearchService,
            DeskPlacementService deskPlacementService) {

        this.deskSearchService = deskSearchService;
        this.deskPlacementService = deskPlacementService;
    }

    @GetMapping("/available")
    public List<DeskResponse> searchAvailableDesks(

            @RequestParam Long floorId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant startTime,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant endTime) {

        DeskSearchRequest request =
                new DeskSearchRequest(
                        floorId,
                        startTime,
                        endTime
                );

        return deskSearchService
                .searchAvailableDesks(request);
    }

    @GetMapping("/recommend")
    public DeskPlacementResponse recommendDesk(

            @RequestParam Long floorId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant startTime,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant endTime,

            @RequestParam Long preferredDeskId) {

        DeskPlacementRequest request =
                new DeskPlacementRequest(
                        floorId,
                        startTime,
                        endTime,
                        preferredDeskId
                );

        return deskPlacementService
                .findNearestDesk(request);
    }
}