package smartdesk.booking.dto.request;

public record LoginRequest(
        String email,
        String password
) {
}