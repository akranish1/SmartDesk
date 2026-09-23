-- =========================================================
-- Smart Desk Booking System
-- V3 - Seed Development/Test Data
-- =========================================================


-- =========================================================
-- 1. Teams
-- =========================================================

INSERT INTO teams (name)
VALUES
    ('Engineering'),
    ('Product');


-- =========================================================
-- 2. Users
-- =========================================================

INSERT INTO users (
    employee_code,
    name,
    email,
    password_hash,
    role,
    team_id,
    active
)
VALUES
    (
        'EMP001',
        'Alice',
        'alice@smartdesk.com',
        'dummy-hash',
        'EMPLOYEE',
        (SELECT id FROM teams WHERE name = 'Engineering'),
        TRUE
    ),
    (
        'EMP002',
        'Bob',
        'bob@smartdesk.com',
        'dummy-hash',
        'EMPLOYEE',
        (SELECT id FROM teams WHERE name = 'Engineering'),
        TRUE
    );


-- =========================================================
-- 3. Floors
-- =========================================================

INSERT INTO floors (
    building,
    floor_number,
    name,
    timezone
)
VALUES
    (
        'Building A',
        1,
        'Engineering Floor',
        'Asia/Kolkata'
    ),
    (
        'Building A',
        2,
        'Product Floor',
        'Asia/Kolkata'
    );


-- =========================================================
-- 4. Floor-Team Quotas
-- =========================================================

INSERT INTO floor_team_quotas (
    floor_id,
    team_id,
    max_concurrent_bookings
)
VALUES
    (
        (SELECT id
         FROM floors
         WHERE building = 'Building A'
           AND floor_number = 1),

        (SELECT id
         FROM teams
         WHERE name = 'Engineering'),

        10
    ),
    (
        (SELECT id
         FROM floors
         WHERE building = 'Building A'
           AND floor_number = 2),

        (SELECT id
         FROM teams
         WHERE name = 'Product'),

        10
    );


-- =========================================================
-- 5. Desks
-- =========================================================

INSERT INTO desks (
    floor_id,
    desk_number,
    x_coordinate,
    y_coordinate,
    desk_type,
    status,
    assigned_user_id
)
VALUES

    -- Active HOT desk
    (
        (SELECT id
         FROM floors
         WHERE building = 'Building A'
           AND floor_number = 1),

        'D101',
        1,
        1,
        'HOT',
        'ACTIVE',
        NULL
    ),

    -- Active HOT desk
    (
        (SELECT id
         FROM floors
         WHERE building = 'Building A'
           AND floor_number = 1),

        'D102',
        2,
        1,
        'HOT',
        'ACTIVE',
        NULL
    ),

    -- Inactive HOT desk
    (
        (SELECT id
         FROM floors
         WHERE building = 'Building A'
           AND floor_number = 1),

        'D103',
        3,
        1,
        'HOT',
        'INACTIVE',
        NULL
    ),

    -- Active HOT desk
    (
        (SELECT id
         FROM floors
         WHERE building = 'Building A'
           AND floor_number = 1),

        'D104',
        4,
        1,
        'HOT',
        'ACTIVE',
        NULL
    );


-- =========================================================
-- 6. Confirmed booking on D102
--
-- Current test booking:
-- 2026-09-23 10:00 UTC
-- to
-- 2026-09-23 12:00 UTC
-- =========================================================

INSERT INTO bookings (
    user_id,
    desk_id,
    start_time,
    end_time,
    status
)
VALUES
    (
        (
            SELECT id
            FROM users
            WHERE employee_code = 'EMP001'
        ),

        (
            SELECT id
            FROM desks
            WHERE desk_number = 'D102'
              AND floor_id = (
                SELECT id
                FROM floors
                WHERE building = 'Building A'
                  AND floor_number = 1
            )
        ),

        '2026-09-23 10:00:00+00',
        '2026-09-23 12:00:00+00',

        'CONFIRMED'
    );