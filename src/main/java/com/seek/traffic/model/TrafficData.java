package com.seek.traffic.model;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TrafficData {

    private LocalDateTime timestamp;
    private int carsCount;
    private int Count;

    public boolean isValid() {
        return timestamp != null && carsCount >= 0;
    }
}