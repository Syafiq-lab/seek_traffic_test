
package com.seek.traffic.batch;

import com.seek.traffic.model.DayTrafficSummary;
import com.seek.traffic.model.LeastTrafficPeriod;
import com.seek.traffic.model.TopHalfHour;
import com.seek.traffic.model.TrafficData;
import com.seek.traffic.writer.ConsoleWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrafficDataWriter implements ItemWriter<Object> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int CONSECUTIVE_PERIODS_FOR_ANALYSIS = 3;

    private final TrafficDataProcessor processor;
    private final ConsoleWriter consoleWriter;
    private final AtomicBoolean hasWritten = new AtomicBoolean(false);

    @Override
    public void write(@NonNull Chunk<? extends Object> chunk) throws Exception {
        if (!hasWritten.compareAndSet(false, true)) {
            log.debug("Traffic analysis results already written, skipping duplicate write operation");
            return;
        }

        try {
            log.info("Starting traffic analysis results output generation");

            writeTotalCarsCount();
            writeDailyTrafficSummaries();
            writeTopHalfHourPeriods();
            writeLeastTrafficPeriod();

            log.info("Successfully completed traffic analysis results output");

        } catch (Exception e) {
            log.error("Failed to write traffic analysis results", e);
            hasWritten.set(false);
            throw new TrafficDataWriteException("Error writing traffic analysis results", e);
        }
    }

    private void writeTotalCarsCount() {
        try {
            log.debug("Writing total cars count");

            List<TrafficData> allTrafficData = processor.getAllTrafficData();
            int totalCars = allTrafficData.stream()
                    .mapToInt(TrafficData::getCarsCount)
                    .sum();

            consoleWriter.writeLine("Total number of cars seen:");
            consoleWriter.writeLine(String.valueOf(totalCars));

            log.debug("Completed writing total cars count: {}", totalCars);
        } catch (Exception e) {
            log.error("Error writing total cars count", e);
            throw new TrafficDataWriteException("Failed to write total cars count", e);
        }
    }

    private void writeDailyTrafficSummaries() {
        try {
            log.debug("Writing daily traffic summaries");

            Map<LocalDate, Integer> dailyCounts = processor.getDailyCarsCount();

            consoleWriter.writeLine("Daily traffic summary:");

            if (dailyCounts.isEmpty()) {
                log.warn("No daily traffic data available for output");
                consoleWriter.writeLine("No daily traffic data available");
                return;
            }

            // Sort by date and write each summary
            dailyCounts.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        String formattedOutput = String.format("%s %d",
                                entry.getKey().format(DATE_FORMATTER),
                                entry.getValue());
                        consoleWriter.writeLine(formattedOutput);
                    });

            log.debug("Completed writing {} daily traffic summaries", dailyCounts.size());
        } catch (Exception e) {
            log.error("Error writing daily traffic summaries", e);
            throw new TrafficDataWriteException("Failed to write daily traffic summaries", e);
        }
    }

    private void writeTopHalfHourPeriods() {
        try {
            log.debug("Writing top half-hour periods");

            List<TopHalfHour> topHalfHours = processor.getTopHalfHours();

            consoleWriter.writeLine("Top 3 half-hour periods with most cars:");

            if (topHalfHours.isEmpty()) {
                log.warn("No half-hour traffic data available for output");
                consoleWriter.writeLine("No half-hour traffic data available");
                return;
            }

            topHalfHours.stream()
                    .limit(3)
                    .forEach(halfHour -> {
                        String formattedOutput = String.format("%s %d",
                                halfHour.getTimestamp().format(TIMESTAMP_FORMATTER),
                                halfHour.getCarsCount());
                        consoleWriter.writeLine(formattedOutput);
                    });

            log.debug("Completed writing top {} half-hour periods", Math.min(3, topHalfHours.size()));
        } catch (Exception e) {
            log.error("Error writing top half-hour periods", e);
            throw new TrafficDataWriteException("Failed to write top half-hour periods", e);
        }
    }

    private void writeLeastTrafficPeriod() {
        try {
            log.debug("Calculating and writing least traffic period");

            List<TrafficData> trafficData = processor.getAllTrafficData();

            consoleWriter.writeLine("1.5-hour period with least cars:");

            if (trafficData.size() < CONSECUTIVE_PERIODS_FOR_ANALYSIS) {
                log.warn("Insufficient traffic data for 1.5-hour period analysis. Required: {}, Available: {}",
                        CONSECUTIVE_PERIODS_FOR_ANALYSIS, trafficData.size());
                consoleWriter.writeLine("Insufficient data for 1.5-hour period analysis");
                return;
            }

            Optional<LeastTrafficPeriod> leastTrafficPeriod = findLeastTrafficPeriod(trafficData);

            if (leastTrafficPeriod.isPresent()) {
                LeastTrafficPeriod period = leastTrafficPeriod.get();
                String formattedOutput = String.format("%s %d",
                        period.getStartTimestamp().format(TIMESTAMP_FORMATTER),
                        period.getTotalCars());
                consoleWriter.writeLine(formattedOutput);

                log.debug("Completed writing least traffic period: {} with {} cars",
                        period.getStartTimestamp(), period.getTotalCars());
            } else {
                log.error("Failed to calculate least traffic period despite sufficient data");
                consoleWriter.writeLine("Error calculating least traffic period");
            }
        } catch (Exception e) {
            log.error("Error writing least traffic period", e);
            throw new TrafficDataWriteException("Failed to write least traffic period", e);
        }
    }

    private Optional<LeastTrafficPeriod> findLeastTrafficPeriod(List<TrafficData> trafficData) {
        if (trafficData.size() < CONSECUTIVE_PERIODS_FOR_ANALYSIS) {
            return Optional.empty();
        }

        // Sort traffic data by timestamp to ensure proper consecutive analysis
        List<TrafficData> sortedData = trafficData.stream()
                .sorted(Comparator.comparing(TrafficData::getTimestamp))
                .collect(Collectors.toList());

        int minTotalCars = Integer.MAX_VALUE;
        LocalDateTime optimalStartTime = null;

        for (int i = 0; i <= sortedData.size() - CONSECUTIVE_PERIODS_FOR_ANALYSIS; i++) {
            int currentWindowSum = calculateWindowSum(sortedData, i, CONSECUTIVE_PERIODS_FOR_ANALYSIS);

            if (currentWindowSum < minTotalCars) {
                minTotalCars = currentWindowSum;
                optimalStartTime = sortedData.get(i + 1).getTimestamp(); // Use middle period timestamp
            }
        }

        if (optimalStartTime == null) {
            return Optional.empty();
        }

        return Optional.of(LeastTrafficPeriod.builder()
                .startTimestamp(optimalStartTime)
                .totalCars(minTotalCars)
                .build());
    }

    private int calculateWindowSum(List<TrafficData> trafficData, int startIndex, int windowSize) {
        return trafficData.subList(startIndex, startIndex + windowSize)
                .stream()
                .mapToInt(TrafficData::getCarsCount)
                .sum();
    }

    public void reset() {
        hasWritten.set(false);
        log.debug("TrafficDataWriter state reset");
    }

    public static class TrafficDataWriteException extends RuntimeException {
        public TrafficDataWriteException(String message, Throwable cause) {
            super(message, cause);
        }

        public TrafficDataWriteException(String message) {
            super(message);
        }
    }
}