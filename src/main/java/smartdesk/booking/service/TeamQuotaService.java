package smartdesk.booking.service;

import smartdesk.booking.exception.InvalidBookingException;
import smartdesk.booking.exception.QuotaExceededException;
import smartdesk.booking.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import smartdesk.booking.entity.Floor;
import smartdesk.booking.entity.FloorTeamQuota;
import smartdesk.booking.entity.Team;
import smartdesk.booking.repository.BookingRepository;
import smartdesk.booking.repository.FloorTeamQuotaRepository;

import java.time.Instant;

@Service
public class TeamQuotaService {

    private final FloorTeamQuotaRepository floorTeamQuotaRepository;
    private final BookingRepository bookingRepository;

    public TeamQuotaService(
            FloorTeamQuotaRepository floorTeamQuotaRepository,
            BookingRepository bookingRepository) {

        this.floorTeamQuotaRepository = floorTeamQuotaRepository;
        this.bookingRepository = bookingRepository;
    }

    public boolean hasQuotaAvailable(
            Team team,
            Floor floor,
            Instant startTime,
            Instant endTime) {

        validateRequest(
                team,
                floor,
                startTime,
                endTime
        );

        FloorTeamQuota quota =
                floorTeamQuotaRepository
                        .findByFloorAndTeam(floor, team)
                        .orElseThrow(() ->
                                new  ResourceNotFoundException(
                                        "No quota configured for team "
                                                + team.getName()
                                                + " on floor "
                                                + floor.getName()
                                ));

        long currentBookings =
                bookingRepository.countConcurrentBookings(
                        team,
                        floor,
                        startTime,
                        endTime
                );

        return currentBookings
                < quota.getMaxConcurrentBookings();
    }

    private void validateRequest(
            Team team,
            Floor floor,
            Instant startTime,
            Instant endTime) {

        if (team == null) {
            throw new InvalidBookingException(
                    "Team is required"
            );
        }

        if (floor == null) {
            throw new InvalidBookingException(
                    "Floor is required"
            );
        }

        if (startTime == null || endTime == null) {
            throw new InvalidBookingException(
                    "Start time and end time are required"
            );
        }

        if (!startTime.isBefore(endTime)) {
            throw new InvalidBookingException(
                    "Start time must be before end time"
            );
        }
    }
    public boolean hasQuotaAvailableForUpdate(
            Team team,
            Floor floor,
            Instant startTime,
            Instant endTime) {

        validateRequest(
                team,
                floor,
                startTime,
                endTime
        );

        FloorTeamQuota quota =
                floorTeamQuotaRepository
                        .findByFloorAndTeamForUpdate(
                                floor,
                                team
                        )
                        .orElseThrow(() ->
                                new  ResourceNotFoundException(
                                        "No quota configured for team "
                                                + team.getName()
                                                + " on floor "
                                                + floor.getName()
                                ));

        long currentBookings =
                bookingRepository.countConcurrentBookings(
                        team,
                        floor,
                        startTime,
                        endTime
                );

        return currentBookings
                < quota.getMaxConcurrentBookings();
    }
}