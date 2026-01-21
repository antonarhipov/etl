package org.example.etl.model;

import java.time.LocalDateTime;

/**
 * Represents a temperature reading extracted from CSV data.
 * The combination of name and datetime forms a unique identifier.
 *
 * @param name     the location or sensor name
 * @param datetime the timestamp of the reading
 * @param temp     the temperature value
 */
public record TemperatureData(
        String name,
        LocalDateTime datetime,
        Double temp
) {
}
