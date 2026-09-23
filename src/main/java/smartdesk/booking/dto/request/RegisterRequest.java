package smartdesk.booking.dto.request;

public record RegisterRequest(
        String employeeCode,
        String name,
        String email,
        String password,
        Long teamId
) {
}