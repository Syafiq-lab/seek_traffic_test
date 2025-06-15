package com.seek.traffic.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeastTrafficPeriod {

    private LocalDateTime startTimestamp;
    private int totalCars;

    public boolean isValid() {
        return startTimestamp != null && totalCars >= 0;
    }
}