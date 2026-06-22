package com.backend.backend.repository;

import com.backend.backend.model.entity.Tour;
import com.backend.backend.model.entity.TourLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JpaRepository<TourLog, Integer> gives us save / findById / delete / etc. for free.
// We only have to declare the custom queries we actually need.
public interface TourLogRepository extends JpaRepository<TourLog, Integer> {

    // all logs that belong to a given tour
    List<TourLog> findByTour(Tour tour);

    // used when we delete a tour and want to wipe its logs first
    void deleteByTour(Tour tour);
}
