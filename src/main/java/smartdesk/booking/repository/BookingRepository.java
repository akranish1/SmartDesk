package smartdesk.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import smartdesk.booking.entity.*;

import java.time.Instant;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByDeskAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            Desk desk,
            BookingStatus status,
            Instant endTime,
            Instant startTime
    );

    boolean existsByDeskAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            Desk desk,
            BookingStatus status,
            Instant endTime,
            Instant startTime
    );
    @Query("""
    SELECT COUNT(b)
    FROM Booking b
    WHERE b.user.team = :team
      AND b.desk.floor = :floor
      AND b.status = smartdesk.booking.entity.BookingStatus.CONFIRMED
      AND b.startTime < :endTime
      AND b.endTime > :startTime
    """)
    long countConcurrentBookings(
            @Param("team") Team team,
            @Param("floor") Floor floor,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime
    );
    @Query("""
    SELECT b
    FROM Booking b
    WHERE b.status = :status
      AND b.startTime <= :cutoff
      AND b.checkedInAt IS NULL
""")
    List<Booking> findBookingsEligibleForNoShow(
            @Param("status") BookingStatus status,
            @Param("cutoff") Instant cutoff
    );
}