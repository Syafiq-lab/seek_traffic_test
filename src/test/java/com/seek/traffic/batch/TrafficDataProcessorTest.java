package com.seek.traffic.batch;

import com.seek.traffic.model.TopHalfHour;
import com.seek.traffic.model.TrafficData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Traffic Data Processor Tests")
class TrafficDataProcessorTest {

    private TrafficDataProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TrafficDataProcessor();
    }

    @Test
    @DisplayName("Should process valid traffic data successfully")
    void shouldProcessValidTrafficData() throws Exception {
        TrafficData trafficData = TrafficData.builder()
                .timestamp(LocalDateTime.of(2023, 12, 1, 10, 30))
                .carsCount(25)
                .build();

        Object result = processor.process(trafficData);

        assertNotNull(result);
        assertEquals(trafficData, result);
        assertEquals(1, processor.getDailyCarsCount().size());
        assertEquals(25, processor.getDailyCarsCount().get(trafficData.getTimestamp().toLocalDate()));
    }

    @Test
    @DisplayName("Should handle null traffic data gracefully")
    void shouldHandleNullTrafficData() throws Exception {
        Object result = processor.process(null);

        assertNull(result);
        assertTrue(processor.getDailyCarsCount().isEmpty());
        assertTrue(processor.getTopHalfHours().isEmpty());
    }

    @Test
    @DisplayName("Should accumulate daily car counts correctly")
    void shouldAccumulateDailyCarCounts() throws Exception {
        LocalDateTime date = LocalDateTime.of(2023, 12, 1, 10, 30);
        TrafficData data1 = TrafficData.builder().timestamp(date).carsCount(10).build();
        TrafficData data2 = TrafficData.builder().timestamp(date.plusHours(1)).carsCount(15).build();

        processor.process(data1);
        processor.process(data2);

        assertEquals(25, processor.getDailyCarsCount().get(date.toLocalDate()));
    }

    @Test
    @DisplayName("Should maintain top half-hour periods correctly")
    void shouldMaintainTopHalfHours() throws Exception {
        TrafficData data1 = TrafficData.builder()
                .timestamp(LocalDateTime.of(2023, 12, 1, 10, 30))
                .carsCount(10)
                .build();
        TrafficData data2 = TrafficData.builder()
                .timestamp(LocalDateTime.of(2023, 12, 1, 11, 30))
                .carsCount(25)
                .build();

        processor.process(data1);
        processor.process(data2);

        List<TopHalfHour> topHalfHours = processor.getTopHalfHours();
        assertEquals(2, topHalfHours.size());
        assertEquals(25, topHalfHours.get(0).getCarsCount());
    }

    @Test
    @DisplayName("Should reset statistics correctly")
    void shouldResetStatistics() throws Exception {
        TrafficData trafficData = TrafficData.builder()
                .timestamp(LocalDateTime.of(2023, 12, 1, 10, 30))
                .carsCount(25)
                .build();
        processor.process(trafficData);

        processor.reset();

        assertTrue(processor.getDailyCarsCount().isEmpty());
        assertTrue(processor.getTopHalfHours().isEmpty());
        assertTrue(processor.getAllTrafficData().isEmpty());
    }

    @Test
    @DisplayName("Should handle invalid traffic data")
    void shouldHandleInvalidTrafficData() throws Exception {
        TrafficData invalidData = TrafficData.builder()
                .timestamp(null)
                .carsCount(-5)
                .build();

        Object result = processor.process(invalidData);

        assertNull(result);
        assertTrue(processor.getDailyCarsCount().isEmpty());
    }

    @Test
    @DisplayName("Should store all traffic data for analysis")
    void shouldStoreAllTrafficDataForAnalysis() throws Exception {
        TrafficData data1 = TrafficData.builder()
                .timestamp(LocalDateTime.of(2023, 12, 1, 10, 30))
                .carsCount(10)
                .build();
        TrafficData data2 = TrafficData.builder()
                .timestamp(LocalDateTime.of(2023, 12, 1, 11, 30))
                .carsCount(15)
                .build();

        processor.process(data1);
        processor.process(data2);

        List<TrafficData> allData = processor.getAllTrafficData();
        assertEquals(2, allData.size());
        assertTrue(allData.contains(data1));
        assertTrue(allData.contains(data2));
    }

    @Test
    @DisplayName("Should handle concurrent processing")
    void shouldHandleConcurrentProcessing() throws Exception {
        TrafficData data1 = TrafficData.builder()
                .timestamp(LocalDateTime.of(2023, 12, 1, 10, 30))
                .carsCount(10)
                .build();
        TrafficData data2 = TrafficData.builder()
                .timestamp(LocalDateTime.of(2023, 12, 1, 10, 30))
                .carsCount(15)
                .build();

        processor.process(data1);
        processor.process(data2);

        assertEquals(25, processor.getDailyCarsCount().get(data1.getTimestamp().toLocalDate()));
        assertEquals(2, processor.getAllTrafficData().size());
    }

    @Test
    @DisplayName("Should sort top half hours by car count descending")
    void shouldSortTopHalfHoursByCarCountDescending() throws Exception {
        TrafficData data1 = TrafficData.builder()
                .timestamp(LocalDateTime.of(2023, 12, 1, 10, 30))
                .carsCount(10)
                .build();
        TrafficData data2 = TrafficData.builder()
                .timestamp(LocalDateTime.of(2023, 12, 1, 11, 30))
                .carsCount(30)
                .build();
        TrafficData data3 = TrafficData.builder()
                .timestamp(LocalDateTime.of(2023, 12, 1, 12, 30))
                .carsCount(20)
                .build();

        processor.process(data1);
        processor.process(data2);
        processor.process(data3);

        List<TopHalfHour> topHalfHours = processor.getTopHalfHours();
        assertEquals(30, topHalfHours.get(0).getCarsCount());
        assertEquals(20, topHalfHours.get(1).getCarsCount());
        assertEquals(10, topHalfHours.get(2).getCarsCount());
    }
}