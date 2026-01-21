package org.example.etl.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TemperatureData record.
 */
class TemperatureDataTest {

    @Test
    void testRecordCreation() {
        // Given
        String name = "Location-A";
        LocalDateTime datetime = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
        Double temp = 22.5;

        // When
        TemperatureData data = new TemperatureData(name, datetime, temp);

        // Then
        assertThat(data.name()).isEqualTo(name);
        assertThat(data.datetime()).isEqualTo(datetime);
        assertThat(data.temp()).isEqualTo(temp);
    }

    @Test
    void testRecordEquality() {
        // Given
        LocalDateTime datetime = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
        TemperatureData data1 = new TemperatureData("Location-A", datetime, 22.5);
        TemperatureData data2 = new TemperatureData("Location-A", datetime, 22.5);

        // Then
        assertThat(data1).isEqualTo(data2);
        assertThat(data1.hashCode()).isEqualTo(data2.hashCode());
    }

    @Test
    void testRecordInequality() {
        // Given
        LocalDateTime datetime = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
        TemperatureData data1 = new TemperatureData("Location-A", datetime, 22.5);
        TemperatureData data2 = new TemperatureData("Location-B", datetime, 22.5);

        // Then
        assertThat(data1).isNotEqualTo(data2);
    }

    @Test
    void testRecordToString() {
        // Given
        LocalDateTime datetime = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
        TemperatureData data = new TemperatureData("Location-A", datetime, 22.5);

        // Then
        String str = data.toString();
        assertThat(str).contains("Location-A");
        assertThat(str).contains("22.5");
    }
}
