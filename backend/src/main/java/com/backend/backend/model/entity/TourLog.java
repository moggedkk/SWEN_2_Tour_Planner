package com.backend.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// A TourLog is one "entry" the user writes after completing a tour.
// It belongs to exactly one Tour (many logs per tour).
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tour_logs")
public class TourLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    // when the tour was done
    @Column(name = "date_time")
    private LocalDateTime dateTime;

    private String comment;

    // how hard the tour felt — kept as plain text ("easy", "medium", "hard")
    // we don't need a separate Difficulty entity for logs to keep things simple
    private String difficulty;

    @Column(name = "total_distance")
    private double totalDistance;

    // total time in minutes (matches the frontend's "duration" field)
    @Column(name = "total_time")
    private double totalTime;

    // 1 to 5 stars
    private int rating;

    // the Tour this log belongs to.
    // ManyToOne = many logs can point to one tour
    @ManyToOne
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;
}
