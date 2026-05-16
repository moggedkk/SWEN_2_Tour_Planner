package com.backend.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TourResponse {
    private int id;
    private String name;
    private String start;
    private String end;
    private String description;
    private String difficulty;
    private String transportType;
    private double distance;
    private int estimatedTime;
}
