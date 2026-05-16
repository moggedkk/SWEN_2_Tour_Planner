package com.backend.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TourRequest {
    private String name;
    private String start;
    private String end;
    private String description;
    private String difficulty;
    private String transportType;
    private double distance;
    private int estimatedTime;
}
