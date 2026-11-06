CREATE DATABASE IF NOT EXISTS crew_management;
USE crew_management;

CREATE TABLE IF NOT EXISTS crew (
    crew_id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) DEFAULT NULL,
    role VARCHAR(30) NOT NULL,
    phone VARCHAR(15) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL,
    availability VARCHAR(20) DEFAULT 'Available',
    PRIMARY KEY (crew_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS flight (
    flight_id INT NOT NULL AUTO_INCREMENT,
    flight_number VARCHAR(20) NOT NULL,
    source VARCHAR(50) NOT NULL,
    destination VARCHAR(50) NOT NULL,
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME NOT NULL,
    source_zone_id VARCHAR(64) NOT NULL,
    destination_zone_id VARCHAR(64) NOT NULL,
    PRIMARY KEY (flight_id),
    UNIQUE KEY flight_number (flight_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS assignment (
    assignment_id INT NOT NULL AUTO_INCREMENT,
    crew_id INT NOT NULL,
    flight_id INT NOT NULL,
    assignment_date DATE NOT NULL,
    PRIMARY KEY (assignment_id),
    UNIQUE KEY crew_flight_unique (crew_id, flight_id),
    KEY flight_id (flight_id),
    CONSTRAINT fk_assignment_crew FOREIGN KEY (crew_id) REFERENCES crew (crew_id) ON DELETE CASCADE,
    CONSTRAINT fk_assignment_flight FOREIGN KEY (flight_id) REFERENCES flight (flight_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS leave_record (
    leave_id INT NOT NULL AUTO_INCREMENT,
    crew_id INT NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    reason VARCHAR(255) DEFAULT NULL,
    status VARCHAR(20) DEFAULT 'Pending',
    PRIMARY KEY (leave_id),
    KEY crew_id (crew_id),
    CONSTRAINT fk_leave_crew FOREIGN KEY (crew_id) REFERENCES crew (crew_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

