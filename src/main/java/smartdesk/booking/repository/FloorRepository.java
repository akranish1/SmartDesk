package smartdesk.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smartdesk.booking.entity.Floor;

import java.util.Optional;

public interface FloorRepository extends JpaRepository<Floor, Long> {

    Optional<Floor> findByBuildingAndFloorNumber(
            String building,
            Integer floorNumber
    );
}