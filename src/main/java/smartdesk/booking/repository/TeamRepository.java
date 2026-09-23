package smartdesk.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smartdesk.booking.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
}