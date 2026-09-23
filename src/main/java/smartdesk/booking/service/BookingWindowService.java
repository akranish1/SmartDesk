package smartdesk.booking.service;

import org.springframework.stereotype.Service;
import smartdesk.booking.exception.InvalidBookingException;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class BookingWindowService {

    private static final Duration MINIMUM_ADVANCE =
            Duration.ofMinutes(30);

    private static final Duration MAXIMUM_HORIZON =
            Duration.ofDays(14);

    public void validate(
            Instant startTime,
            Instant endTime,
            String timezone) {

        Instant now = Instant.now();

        // Booking must be at least 30 minutes in advance
        if (startTime.isBefore(
                now.plus(MINIMUM_ADVANCE))) {

            throw new InvalidBookingException(
                    "Booking must be made at least 30 minutes before start time"
            );
        }

        // Booking cannot be more than 14 days in advance
        if (startTime.isAfter(
                now.plus(MAXIMUM_HORIZON))) {

            throw new InvalidBookingException(
                    "Booking cannot be made more than 14 days in advance"
            );
        }

        // Validate the floor timezone
        ZoneId zoneId;

        try {
            zoneId = ZoneId.of(timezone);
        } catch (Exception ex) {
            throw new InvalidBookingException(
                    "Invalid floor timezone: " + timezone
            );
        }

        // Convert to floor-local time for future local-time rules
        ZonedDateTime localStart =
                startTime.atZone(zoneId);

        ZonedDateTime localEnd =
                endTime.atZone(zoneId);

        // Prevent an invalid local interval
        if (!localStart.isBefore(localEnd)) {
            throw new InvalidBookingException(
                    "Booking time interval is invalid"
            );
        }
    }
}