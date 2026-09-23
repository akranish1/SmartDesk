package smartdesk.booking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import smartdesk.booking.dto.response.AuthResponse;
import smartdesk.booking.dto.request.LoginRequest;
import smartdesk.booking.dto.request.RegisterRequest;
import smartdesk.booking.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(
            @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request
    ) {
        return authService.authenticate(request);
    }
}