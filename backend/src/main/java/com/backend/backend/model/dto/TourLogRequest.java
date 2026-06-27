package com.backend.backend.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// What the frontend SENDS us when creating or updating a tour log.
// We don't include id or tourId here — those come from the URL path.
//
// Constraints mirror the form rules in main.ts / profile.html. imageName / imageEncoded
// are optional (only set when the user actually attached a file).
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TourLogRequest {

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDateTime dateTime;

    @Size(max = 1000, message = "Comment must be at most 1000 characters")
    private String comment;

    @NotBlank(message = "Difficulty rating is required")
    private String difficulty;

    @Min(value = 0, message = "Distance cannot be negative")
    private double totalDistance;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private double totalTime;

    @Min(value = 0, message = "Rating must be between 0 and 5")
    @Max(value = 5, message = "Rating must be between 0 and 5")
    private int rating;

    // optional — only present on uploads, ignored if null
    private String imageName;
    private String imageEncoded;
}
