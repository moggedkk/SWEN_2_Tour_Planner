package com.backend.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Constraints mirror the frontend form rules (see createtours.html / profile.html).
// Anything that gets past the UI also has to pass these — protects us from curl /
// Postman / a buggy client.
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TourRequest {

    @NotBlank(message = "Tour name is required")
    @Size(min = 3, max = 100, message = "Tour name must be 3-100 characters")
    private String name;

    @NotBlank(message = "Start location is required")
    @Size(min = 2, max = 100, message = "Start location must be 2-100 characters")
    private String start;

    @NotBlank(message = "End location is required")
    @Size(min = 2, max = 100, message = "End location must be 2-100 characters")
    private String end;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500, message = "Description must be 10-500 characters")
    private String description;

    // difficulty + transportType are validated against the allowed set in the service
    // layer because they're tied to business enums, not just string length
    @NotBlank(message = "Difficulty is required")
    private String difficulty;

    @NotBlank(message = "Transport type is required")
    private String transportType;
}
