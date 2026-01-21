-- Temperature data table for storing imported readings
CREATE TABLE IF NOT EXISTS temperature_data (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    datetime TIMESTAMP NOT NULL,
    temp DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_name_datetime UNIQUE (name, datetime)
);
