package com.seek.traffic.batch;

import com.seek.traffic.model.TopHalfHour;
import com.seek.traffic.model.TrafficData;
import com.seek.traffic.writer.ConsoleWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.item.Chunk;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Traffic Data Writer Tests")
class TrafficDataWriterTest {

    @Mock
    private TrafficDataProcessor processor;

    @Mock
    private ConsoleWriter consoleWriter;

    private TrafficDataWriter writer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        writer = new TrafficDataWriter(processor, consoleWriter);
    }

    @Test
    @DisplayName("Should write complete traffic analysis")
    void shouldWriteCompleteTrafficAnalysis() throws Exception {
        setupMockData();

        Chunk<Object> chunk = new Chunk<>();

        writer.write(chunk);

        verify(consoleWriter, times(1)).writeLine("Total number of cars seen:");
        verify(consoleWriter, times(1)).writeLine("100");
        verify(consoleWriter, times(1)).writeLine("Daily traffic summary:");
        verify(consoleWriter, times(1)).writeLine("Top 3 half-hour periods with most cars:");
        verify(consoleWriter, times(1)).writeLine("1.5-hour period with least cars:");
    }

    @Test
    @DisplayName("Should write only once per execution")
    void shouldWriteOnlyOncePerExecution() throws Exception {
        setupMockData();

        Chunk<Object> chunk = new Chunk<>();

        writer.write(chunk);
        writer.write(chunk);

        verify(consoleWriter, times(1)).writeLine("Total number of cars seen:");
    }

    @Test
    @DisplayName("Should handle empty traffic data")
    void shouldHandleEmptyTrafficData() throws Exception {
        when(processor.getAllTrafficData()).thenReturn(List.of());
        when(processor.getDailyCarsCount()).thenReturn(Map.of());
        when(processor.getTopHalfHours()).thenReturn(List.of());

        Chunk<Object> chunk = new Chunk<>();

        writer.write(chunk);

        verify(consoleWriter, times(1)).writeLine("0");
        verify(consoleWriter, times(1)).writeLine("No daily traffic data available");
        verify(consoleWriter, times(1)).writeLine("No half-hour traffic data available");
        verify(consoleWriter, times(1)).writeLine("Insufficient data for 1.5-hour period analysis");
    }

    @Test
    @DisplayName("Should format dates correctly")
    void shouldFormatDatesCorrectly() throws Exception {
        LocalDate testDate = LocalDate.of(2023, 12, 1);
        LocalDateTime testDateTime = LocalDateTime.of(2023, 12, 1, 10, 30);

        when(processor.getAllTrafficData()).thenReturn(List.of(
                TrafficData.builder().timestamp(testDateTime).carsCount(50).build()
        ));
        when(processor.getDailyCarsCount()).thenReturn(Map.of(testDate, 50));
        when(processor.getTopHalfHours()).thenReturn(List.of(
                TopHalfHour.builder().timestamp(testDateTime).carsCount(50).build()
        ));

        Chunk<Object> chunk = new Chunk<>();

        writer.write(chunk);

        verify(consoleWriter, times(1)).writeLine("2023-12-01 50");
        verify(consoleWriter, times(1)).writeLine("2023-12-01 10:30 50");
    }

    @Test
    @DisplayName("Should calculate least traffic period correctly")
    void shouldCalculateLeastTrafficPeriodCorrectly() throws Exception {
        List<TrafficData> trafficData = List.of(
                TrafficData.builder().timestamp(LocalDateTime.of(2023, 12, 1, 10, 0)).carsCount(10).build(),
                TrafficData.builder().timestamp(LocalDateTime.of(2023, 12, 1, 10, 30)).carsCount(5).build(),
                TrafficData.builder().timestamp(LocalDateTime.of(2023, 12, 1, 11, 0)).carsCount(8).build(),
                TrafficData.builder().timestamp(LocalDateTime.of(2023, 12, 1, 11, 30)).carsCount(15).build()
        );

        when(processor.getAllTrafficData()).thenReturn(trafficData);
        when(processor.getDailyCarsCount()).thenReturn(Map.of());
        when(processor.getTopHalfHours()).thenReturn(List.of());

        Chunk<Object> chunk = new Chunk<>();

        writer.write(chunk);

        verify(consoleWriter, times(1)).writeLine("2023-12-01 10:30 23");
    }

    @Test
    @DisplayName("Should handle writer exception and reset state")
    void shouldHandleWriterExceptionAndResetState() throws Exception {
        when(processor.getAllTrafficData()).thenReturn(List.of());
        doThrow(new RuntimeException("Writer error")).when(consoleWriter).writeLine(anyString());

        Chunk<Object> chunk = new Chunk<>();

        assertThrows(TrafficDataWriter.TrafficDataWriteException.class, () -> {
            writer.write(chunk);
        });
    }

    @Test
    @DisplayName("Should reset writer state when reset is called")
    void shouldResetWriterState() throws Exception {
        setupMockData();

        Chunk<Object> chunk = new Chunk<>();

        // Write once
        writer.write(chunk);
        verify(consoleWriter, times(1)).writeLine("Total number of cars seen:");

        // Reset and write again
        writer.reset();
        writer.write(chunk);
        verify(consoleWriter, times(2)).writeLine("Total number of cars seen:");
    }

    @Test
    @DisplayName("Should handle null chunk gracefully")
    void shouldHandleNullChunkGracefully() throws Exception {
        setupMockData();

        writer.write(new Chunk<>());

        verify(consoleWriter, times(1)).writeLine("Total number of cars seen:");
        verify(consoleWriter, times(1)).writeLine("100");
    }

    @Test
    @DisplayName("Should sort daily summaries by date")
    void shouldSortDailySummariesByDate() throws Exception {
        LocalDate date1 = LocalDate.of(2023, 12, 1);
        LocalDate date2 = LocalDate.of(2023, 12, 2);
        LocalDate date3 = LocalDate.of(2023, 11, 30);

        Map<LocalDate, Integer> unsortedData = Map.of(
                date2, 50,
                date1, 30,
                date3, 20
        );

        when(processor.getAllTrafficData()).thenReturn(List.of());
        when(processor.getDailyCarsCount()).thenReturn(unsortedData);
        when(processor.getTopHalfHours()).thenReturn(List.of());

        Chunk<Object> chunk = new Chunk<>();
        writer.write(chunk);

        // Verify dates are written in chronological order
        verify(consoleWriter, times(1)).writeLine("2023-11-30 20");
        verify(consoleWriter, times(1)).writeLine("2023-12-01 30");
        verify(consoleWriter, times(1)).writeLine("2023-12-02 50");
    }

    private void setupMockData() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 12, 1, 10, 30);
        LocalDate date = LocalDate.of(2023, 12, 1);

        List<TrafficData> trafficData = List.of(
                TrafficData.builder().timestamp(dateTime).carsCount(50).build(),
                TrafficData.builder().timestamp(dateTime.plusMinutes(30)).carsCount(30).build(),
                TrafficData.builder().timestamp(dateTime.plusHours(1)).carsCount(20).build()
        );

        List<TopHalfHour> topHalfHours = List.of(
                TopHalfHour.builder().timestamp(dateTime).carsCount(50).build(),
                TopHalfHour.builder().timestamp(dateTime.plusMinutes(30)).carsCount(30).build(),
                TopHalfHour.builder().timestamp(dateTime.plusHours(1)).carsCount(20).build()
        );

        when(processor.getAllTrafficData()).thenReturn(trafficData);
        when(processor.getDailyCarsCount()).thenReturn(Map.of(date, 100));
        when(processor.getTopHalfHours()).thenReturn(topHalfHours);
    }
}