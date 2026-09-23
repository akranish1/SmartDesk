package smartdesk.booking.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import smartdesk.booking.entity.BookingStatus;
import smartdesk.booking.entity.Floor;
import smartdesk.booking.entity.FloorTeamQuota;
import smartdesk.booking.entity.Team;

import java.time.Instant;
import java.util.Optional;

public interface FloorTeamQuotaRepository
        extends JpaRepository<FloorTeamQuota, Long> {

    Optional<FloorTeamQuota> findByFloorAndTeam(
            Floor floor,
            Team team
    );

    @Query("""
        SELECT COUNT(b)
        FROM Booking b
        WHERE b.desk.floor = :floor
          AND b.user.team = :team
          AND b.status = :status
          AND b.startTime < :endTime
          AND b.endTime > :startTime
        """)
    long countOverlappingBookingsForTeamOnFloor(
            @Param("floor") Floor floor,
            @Param("team") Team team,
            @Param("status") BookingStatus status,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT q
    FROM FloorTeamQuota q
    WHERE q.floor = :floor
      AND q.team = :team
    """)
    Optional<FloorTeamQuota> findByFloorAndTeamForUpdate(
            @Param("floor") Floor floor,
            @Param("team") Team team
    );
}