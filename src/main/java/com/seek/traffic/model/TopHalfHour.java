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
public class TopHalfHour {

    private LocalDateTime timestamp;
    private int carsCount;

    public boolean isValid() {
        return timestamp != null && carsCount >= 0;
    }
}