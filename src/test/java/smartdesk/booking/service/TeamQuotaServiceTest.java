package smartdesk.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartdesk.booking.entity.Floor;
import smartdesk.booking.entity.FloorTeamQuota;
import smartdesk.booking.entity.Team;
import smartdesk.booking.repository.BookingRepository;
import smartdesk.booking.repository.FloorTeamQuotaRepository;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamQuotaServiceTest {

    @Mock
    private FloorTeamQuotaRepository floorTeamQuotaRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private TeamQuotaService teamQuotaService;

    @Test
    void shouldReturnTrueWhenQuotaIsAvailable() {

        Team team = mock(Team.class);
        Floor floor = mock(Floor.class);
        FloorTeamQuota quota = mock(FloorTeamQuota.class);

        Instant startTime =
                Instant.parse("2026-09-23T10:00:00Z");

        Instant endTime =
                Instant.parse("2026-09-23T14:00:00Z");

        when(floorTeamQuotaRepository
                .findByFloorAndTeam(floor, team))
                .thenReturn(Optional.of(quota));

        when(quota.getMaxConcurrentBookings())
                .thenReturn(2);

        when(bookingRepository.countConcurrentBookings(
                team,
                floor,
                startTime,
                endTime))
                .thenReturn(1L);

        boolean result =
                teamQuotaService.hasQuotaAvailable(
                        team,
                        floor,
                        startTime,
                        endTime
                );

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenQuotaIsExhausted() {

        Team team = mock(Team.class);
        Floor floor = mock(Floor.class);
        FloorTeamQuota quota = mock(FloorTeamQuota.class);

        Instant startTime =
                Instant.parse("2026-09-23T10:00:00Z");

        Instant endTime =
                Instant.parse("2026-09-23T14:00:00Z");

        when(floorTeamQuotaRepository
                .findByFloorAndTeam(floor, team))
                .thenReturn(Optional.of(quota));

        when(quota.getMaxConcurrentBookings())
                .thenReturn(2);

        when(bookingRepository.countConcurrentBookings(
                team,
                floor,
                startTime,
                endTime))
                .thenReturn(2L);

        boolean result =
                teamQuotaService.hasQuotaAvailable(
                        team,
                        floor,
                        startTime,
                        endTime
                );

        assertFalse(result);
    }

    @Test
    void shouldThrowExceptionWhenQuotaIsNotConfigured() {

        Team team = mock(Team.class);
        Floor floor = mock(Floor.class);

        when(floorTeamQuotaRepository
                .findByFloorAndTeam(floor, team))
                .thenReturn(Optional.empty());

        Instant startTime =
                Instant.parse("2026-09-23T10:00:00Z");

        Instant endTime =
                Instant.parse("2026-09-23T14:00:00Z");

        assertThrows(
                IllegalArgumentException.class,
                () -> teamQuotaService.hasQuotaAvailable(
                        team,
                        floor,
                        startTime,
                        endTime
                )
        );
    }
    @Test
    void shouldCheckQuotaForRequestedTimeInterval() {

        Team team = mock(Team.class);
        Floor floor = mock(Floor.class);
        FloorTeamQuota quota = mock(FloorTeamQuota.class);

        Instant startTime =
                Instant.parse("2026-09-23T12:00:00Z");

        Instant endTime =
                Instant.parse("2026-09-23T14:00:00Z");

        when(floorTeamQuotaRepository
                .findByFloorAndTeam(floor, team))
                .thenReturn(Optional.of(quota));

        when(quota.getMaxConcurrentBookings())
                .thenReturn(2);

        when(bookingRepository.countConcurrentBookings(
                team,
                floor,
                startTime,
                endTime))
                .thenReturn(1L);

        boolean result =
                teamQuotaService.hasQuotaAvailable(
                        team,
                        floor,
                        startTime,
                        endTime
                );

        assertTrue(result);

        verify(bookingRepository).countConcurrentBookings(
                team,
                floor,
                startTime,
                endTime
        );
    }
}