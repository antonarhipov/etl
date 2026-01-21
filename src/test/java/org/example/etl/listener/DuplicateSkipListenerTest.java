package org.example.etl.listener;

import org.example.etl.model.TemperatureData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DuplicateSkipListener.
 */
class DuplicateSkipListenerTest {

    private DuplicateSkipListener listener;

    @BeforeEach
    void setUp() {
        listener = new DuplicateSkipListener();
    }

    @Test
    void testInitialSkipCountIsZero() {
        assertThat(listener.getSkipCount()).isZero();
    }

    @Test
    void testSkipCountIncrementsOnWrite() {
        // Given
        TemperatureData data = new TemperatureData(
                "Location-A",
                LocalDateTime.of(2024, 1, 15, 10, 0, 0),
                22.5
        );

        // When
        listener.onSkipInWrite(data, new RuntimeException("Duplicate key"));

        // Then
        assertThat(listener.getSkipCount()).isEqualTo(1);
    }

    @Test
    void testMultipleSkips() {
        // Given
        TemperatureData data1 = new TemperatureData("Location-A", LocalDateTime.now(), 22.5);
        TemperatureData data2 = new TemperatureData("Location-B", LocalDateTime.now(), 18.3);
        TemperatureData data3 = new TemperatureData("Location-C", LocalDateTime.now(), 15.8);

        // When
        listener.onSkipInWrite(data1, new RuntimeException("Duplicate"));
        listener.onSkipInWrite(data2, new RuntimeException("Duplicate"));
        listener.onSkipInWrite(data3, new RuntimeException("Duplicate"));

        // Then
        assertThat(listener.getSkipCount()).isEqualTo(3);
    }

    @Test
    void testResetClearsCount() {
        // Given
        TemperatureData data = new TemperatureData("Location-A", LocalDateTime.now(), 22.5);
        listener.onSkipInWrite(data, new RuntimeException("Duplicate"));
        assertThat(listener.getSkipCount()).isEqualTo(1);

        // When
        listener.reset();

        // Then
        assertThat(listener.getSkipCount()).isZero();
    }

    @Test
    void testOnSkipInReadDoesNotAffectCount() {
        // When
        listener.onSkipInRead(new RuntimeException("Read error"));

        // Then
        assertThat(listener.getSkipCount()).isZero();
    }

    @Test
    void testOnSkipInProcessDoesNotAffectCount() {
        // Given
        TemperatureData data = new TemperatureData("Location-A", LocalDateTime.now(), 22.5);

        // When
        listener.onSkipInProcess(data, new RuntimeException("Process error"));

        // Then
        assertThat(listener.getSkipCount()).isZero();
    }
}
