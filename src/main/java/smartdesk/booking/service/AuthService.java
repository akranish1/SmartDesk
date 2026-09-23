package smartdesk.booking.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartdesk.booking.dto.request.LoginRequest;
import smartdesk.booking.dto.request.RegisterRequest;
import smartdesk.booking.dto.response.AuthResponse;
import smartdesk.booking.entity.Team;
import smartdesk.booking.entity.User;
import smartdesk.booking.entity.UserRole;
import smartdesk.booking.exception.AuthenticationException;
import smartdesk.booking.exception.InvalidBookingException;
import smartdesk.booking.exception.ResourceNotFoundException;
import smartdesk.booking.repository.TeamRepository;
import smartdesk.booking.repository.UserRepository;
import smartdesk.booking.security.JwtService;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            TeamRepository teamRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (request == null) {
            throw new InvalidBookingException(
                    "Registration request cannot be null"
            );
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new InvalidBookingException(
                    "Email is already registered"
            );
        }

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Team not found: " + request.teamId()
                        )
                );

        String passwordHash =
                passwordEncoder.encode(request.password());

        User user = new User(
                request.employeeCode(),
                request.name(),
                request.email(),
                passwordHash,
                UserRole.EMPLOYEE,
                team
        );

        User savedUser = userRepository.save(user);

        return toAuthResponse(savedUser,null);
    }

    public AuthResponse authenticate(LoginRequest request) {

        if (request == null) {
            throw new AuthenticationException("Login request cannot be null");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
        new AuthenticationException("User account is inactive")
                );

        if (!user.isActive()) {
            throw new AuthenticationException("User account is inactive");
        }

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new AuthenticationException("Invalid email or password");
        }
        String accessToken = jwtService.generateAccessToken(user);

        return toAuthResponse(user,accessToken);
    }

    private AuthResponse toAuthResponse(
            User user,
            String accessToken
    ) {

        return new AuthResponse(
                user.getId(),
                user.getEmployeeCode(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                accessToken
        );
    }
}