package com.seek.traffic.batch;

import com.seek.traffic.model.TrafficData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.ByteArrayResource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Traffic Data Reader Tests")
class TrafficDataReaderTest {

    private TrafficDataReader reader;
    private ExecutionContext executionContext;

    @BeforeEach
    void setUp() {
        reader = new TrafficDataReader();
        executionContext = new ExecutionContext();
    }

    @Test
    @DisplayName("Should initialize reader with correct settings")
    void shouldInitializeReaderWithCorrectSettings() {
        assertNotNull(reader);
        assertEquals("trafficDataReader", reader.getName());
    }

    @Test
    @DisplayName("Should parse valid ISO timestamp format")
    void shouldParseValidISOTimestampFormat() {
        String csvContent = "timestamp,cars_count\n2021-12-01T10:30:00,25\n";
        reader.setResource(new ByteArrayResource(csvContent.getBytes()));

        try {
            reader.open(executionContext);
            TrafficData result = reader.read();

            assertNotNull(result);
            assertEquals(LocalDateTime.of(2021, 12, 1, 10, 30), result.getTimestamp());
            assertEquals(25, result.getCarsCount());
        } catch (Exception e) {
            fail("Should not throw exception for valid data: " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    @Test
    @DisplayName("Should parse valid standard timestamp format")
    void shouldParseValidStandardTimestampFormat() {
        String csvContent = "timestamp,cars_count\n2021-12-01 10:30:00,25\n";
        reader.setResource(new ByteArrayResource(csvContent.getBytes()));

        try {
            reader.open(executionContext);
            TrafficData result = reader.read();

            assertNotNull(result);
            assertEquals(LocalDateTime.of(2021, 12, 1, 10, 30), result.getTimestamp());
            assertEquals(25, result.getCarsCount());
        } catch (Exception e) {
            fail("Should not throw exception for valid data: " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    @Test
    @DisplayName("Should handle zero car count")
    void shouldHandleZeroCarCount() {
        String csvContent = "timestamp,cars_count\n2021-12-01T10:30:00,0\n";
        reader.setResource(new ByteArrayResource(csvContent.getBytes()));

        try {
            reader.open(executionContext);
            TrafficData result = reader.read();

            assertNotNull(result);
            assertEquals(0, result.getCarsCount());
        } catch (Exception e) {
            fail("Should not throw exception for zero car count: " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    @Test
    @DisplayName("Should throw exception for negative car count")
    void shouldThrowExceptionForNegativeCarCount() {
        String csvContent = "timestamp,cars_count\n2021-12-01T10:30:00,-5\n";
        reader.setResource(new ByteArrayResource(csvContent.getBytes()));

        try {
            reader.open(executionContext);
            assertThrows(Exception.class, () -> reader.read());
        } catch (Exception e) {
            // Expected behavior when opening reader with invalid data
            assertTrue(e.getMessage().contains("Cars count cannot be negative") ||
                    e.getCause() != null && e.getCause().getMessage().contains("Cars count cannot be negative"));
        } finally {
            reader.close();
        }
    }

    @Test
    @DisplayName("Should throw exception for invalid timestamp format")
    void shouldThrowExceptionForInvalidTimestampFormat() {
        String csvContent = "timestamp,cars_count\ninvalid-timestamp,25\n";
        reader.setResource(new ByteArrayResource(csvContent.getBytes()));

        try {
            reader.open(executionContext);
            assertThrows(Exception.class, () -> reader.read());
        } catch (Exception e) {
            // Expected behavior when opening reader with invalid data
            assertTrue(e.getMessage().contains("Unable to parse timestamp") ||
                    e.getCause() != null);
        } finally {
            reader.close();
        }
    }

    @Test
    @DisplayName("Should return null when no more data")
    void shouldReturnNullWhenNoMoreData() {
        String csvContent = "timestamp,cars_count\n2021-12-01T10:30:00,25\n";
        reader.setResource(new ByteArrayResource(csvContent.getBytes()));

        try {
            reader.open(executionContext);

            // Read the first (and only) record
            TrafficData result1 = reader.read();
            assertNotNull(result1);

            // Try to read again - should return null
            TrafficData result2 = reader.read();
            assertNull(result2);

        } catch (Exception e) {
            fail("Should not throw exception when no data: " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    @Test
    @DisplayName("Should handle empty file gracefully")
    void shouldHandleEmptyFileGracefully() {
        String csvContent = "timestamp,cars_count\n";
        reader.setResource(new ByteArrayResource(csvContent.getBytes()));

        try {
            reader.open(executionContext);
            TrafficData result = reader.read();
            assertNull(result);
        } catch (Exception e) {
            fail("Should not throw exception for empty file: " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    @Test
    @DisplayName("Should handle multiple records correctly")
    void shouldHandleMultipleRecordsCorrectly() {
        String csvContent = "timestamp,cars_count\n" +
                "2021-12-01T10:30:00,25\n" +
                "2021-12-01T11:00:00,30\n" +
                "2021-12-01T11:30:00,15\n";
        reader.setResource(new ByteArrayResource(csvContent.getBytes()));

        try {
            reader.open(executionContext);

            TrafficData result1 = reader.read();
            assertNotNull(result1);
            assertEquals(25, result1.getCarsCount());

            TrafficData result2 = reader.read();
            assertNotNull(result2);
            assertEquals(30, result2.getCarsCount());

            TrafficData result3 = reader.read();
            assertNotNull(result3);
            assertEquals(15, result3.getCarsCount());

            TrafficData result4 = reader.read();
            assertNull(result4);

        } catch (Exception e) {
            fail("Should not throw exception for multiple records: " + e.getMessage());
        } finally {
            reader.close();
        }
    }
}