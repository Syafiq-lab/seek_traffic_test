package com.seek.traffic.batch;

import com.seek.traffic.model.TopHalfHour;
import com.seek.traffic.model.TrafficData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


@Slf4j
@Component
@Getter
public class TrafficDataProcessor implements ItemProcessor<TrafficData, Object> {

    private final Map<LocalDate, Integer> dailyCarsCount = new ConcurrentHashMap<>();
    private final PriorityQueue<TopHalfHour> topHalfHours = new PriorityQueue<>(
            Comparator.comparingInt(TopHalfHour::getCarsCount).reversed());
    private final List<TrafficData> allTrafficData = new CopyOnWriteArrayList<>();

    @Override
    public Object process(@NonNull TrafficData item) throws Exception {
        if (item == null || !item.isValid()) {
            log.warn("Invalid traffic data item received: {}", item);
            return null;
        }

        try {
            updateDailyCarsCount(item);
            
            updateTopHalfHours(item);
            
            allTrafficData.add(item);
            
            log.trace("Processed traffic data: {} cars at {}", 
                item.getCarsCount(), item.getTimestamp());
            
            return item;
            
        } catch (Exception e) {
            log.error("Error processing traffic data item: {}", item, e);
            throw new TrafficDataProcessingException("Failed to process traffic data", e);
        }
    }


    private void updateDailyCarsCount(TrafficData item) {
        LocalDate date = item.getTimestamp().toLocalDate();
        dailyCarsCount.merge(date, item.getCarsCount(), Integer::sum);
        
        log.trace("Updated daily count for {}: total now {}", 
            date, dailyCarsCount.get(date));
    }


    private void updateTopHalfHours(TrafficData item) {
        TopHalfHour halfHour = TopHalfHour.builder()
                .timestamp(item.getTimestamp())
                .carsCount(item.getCarsCount())
                .build();
        
        synchronized (topHalfHours) {
            topHalfHours.offer(halfHour);
            
            if (topHalfHours.size() > 100) {
                List<TopHalfHour> topList = new ArrayList<>(topHalfHours);
                topList.sort(Comparator.comparingInt(TopHalfHour::getCarsCount).reversed());
                
                topHalfHours.clear();
                topHalfHours.addAll(topList.subList(0, Math.min(50, topList.size())));
            }
        }
        
        log.trace("Added half-hour period: {} cars at {}", 
            item.getCarsCount(), item.getTimestamp());
    }


    public List<TopHalfHour> getTopHalfHours() {
        synchronized (topHalfHours) {
            return topHalfHours.stream()
                    .sorted(Comparator.comparingInt(TopHalfHour::getCarsCount).reversed())
                    .toList();
        }
    }


    public List<TrafficData> getAllTrafficData() {
        return allTrafficData.stream()
                .sorted(Comparator.comparing(TrafficData::getTimestamp))
                .toList();
    }


    public void reset() {
        dailyCarsCount.clear();
        synchronized (topHalfHours) {
            topHalfHours.clear();
        }
        allTrafficData.clear();
        log.debug("Traffic data processor statistics reset");
    }


    public static class TrafficDataProcessingException extends RuntimeException {
        public TrafficDataProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}