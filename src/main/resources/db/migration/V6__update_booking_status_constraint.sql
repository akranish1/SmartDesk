ALTER TABLE bookings
DROP CONSTRAINT ck_booking_status;

ALTER TABLE bookings
    ADD CONSTRAINT ck_booking_status
        CHECK (
            status IN (
                       'CONFIRMED',
                       'CHECKED_IN',
                       'NO_SHOW',
                       'CANCELLED'
                )
            );