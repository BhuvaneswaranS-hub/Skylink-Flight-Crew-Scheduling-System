USE crew_management;

-- This migration matches the project's schema.sql delete behavior and keeps
-- existing rows intact. It changes only what happens on a future parent delete.
-- These legacy constraint names were confirmed in the affected database.
ALTER TABLE assignment
    DROP FOREIGN KEY assignment_ibfk_1,
    ADD CONSTRAINT fk_assignment_crew FOREIGN KEY (crew_id)
        REFERENCES crew (crew_id) ON DELETE CASCADE;

ALTER TABLE assignment
    DROP FOREIGN KEY assignment_ibfk_2,
    ADD CONSTRAINT fk_assignment_flight FOREIGN KEY (flight_id)
        REFERENCES flight (flight_id) ON DELETE CASCADE;

ALTER TABLE leave_record
    DROP FOREIGN KEY leave_record_ibfk_1,
    ADD CONSTRAINT fk_leave_crew FOREIGN KEY (crew_id)
        REFERENCES crew (crew_id) ON DELETE CASCADE;
