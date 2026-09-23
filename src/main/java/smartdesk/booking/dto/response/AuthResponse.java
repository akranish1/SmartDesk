package smartdesk.booking.dto.response;

public record AuthResponse(
        Long userId,
        String employeeCode,
        String name,
        String email,
        String role,
        String accessToken
) {
}