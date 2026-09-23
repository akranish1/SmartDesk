package smartdesk.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import smartdesk.booking.entity.Desk;
import smartdesk.booking.entity.DeskStatus;
import smartdesk.booking.entity.Floor;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;


public interface DeskRepository extends JpaRepository<Desk, Long> {

    List<Desk> findByFloorAndStatus(
            Floor floor,
            DeskStatus status
    );

    @Query("""
        SELECT d
        FROM Desk d
        WHERE d.floor = :floor
          AND d.status = :status
          AND NOT EXISTS (
              SELECT b.id
              FROM Booking b
              WHERE b.desk = d
                AND b.status = smartdesk.booking.entity.BookingStatus.CONFIRMED
                AND b.startTime < :endTime
                AND b.endTime > :startTime
          )
        """)
    List<Desk> findAvailableDesks(
            @Param("floor") Floor floor,
            @Param("status") DeskStatus status,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT d
    FROM Desk d
    WHERE d.id = :deskId
    """)
    Optional<Desk> findByIdForUpdate(
            @Param("deskId") Long deskId
    );
}