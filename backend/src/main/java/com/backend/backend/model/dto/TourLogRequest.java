package com.backend.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// What the frontend SENDS us when creating or updating a tour log.
// We don't include id or tourId here — those come from the URL path.
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TourLogRequest {
    private LocalDateTime dateTime;
    private String comment;
    private String difficulty;
    private double totalDistance;
    private double totalTime;
    private int rating;
}
