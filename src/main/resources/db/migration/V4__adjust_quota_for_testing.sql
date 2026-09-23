UPDATE floor_team_quotas
SET max_concurrent_bookings = 2
WHERE floor_id = (
    SELECT id
    FROM floors
    WHERE building = 'Building A'
      AND floor_number = 1
)
  AND team_id = (
    SELECT id
    FROM teams
    WHERE name = 'Engineering'
);