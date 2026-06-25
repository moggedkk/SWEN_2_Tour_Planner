package com.backend.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// What we SEND BACK to the frontend.
// Includes the id (so the frontend can update/delete it later)
// and tourId (so the frontend knows which tour the log belongs to).
@Getter
@AllArgsConstructor
public class TourLogResponse {
    private int id;
    private int tourId;
    private LocalDateTime dateTime;
    private String comment;
    private String difficulty;
    private double totalDistance;
    private double totalTime;
    private int rating;
    private String imageName;
    private String imagePath;
}
