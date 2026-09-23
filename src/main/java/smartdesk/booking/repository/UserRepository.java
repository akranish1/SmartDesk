package smartdesk.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smartdesk.booking.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmployeeCode(String employeeCode);

    Optional<User> findByEmail(String email);
}