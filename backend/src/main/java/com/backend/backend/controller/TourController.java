package com.backend.backend.controller;

import com.backend.backend.model.dto.TourRequest;
import com.backend.backend.model.dto.TourResponse;
import com.backend.backend.service.declaration.ITourService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tours")
@CrossOrigin
public class TourController {

    private final ITourService tourService;

    public TourController(ITourService tourService) {
        this.tourService = tourService;
    }

    @PostMapping
    public ResponseEntity<TourResponse> create(@RequestBody TourRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User '{}' creating tour '{}'", username, request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(tourService.createTour(request, username));
    }

    @GetMapping
    public ResponseEntity<List<TourResponse>> getAll() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("User '{}' fetching all tours", username);
        return ResponseEntity.ok(tourService.getAllTours(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourResponse> getOne(@PathVariable int id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("User '{}' fetching tour id={}", username, id);
        return ResponseEntity.ok(tourService.getTour(id, username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourResponse> update(@PathVariable int id, @RequestBody TourRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User '{}' updating tour id={}", username, id);
        return ResponseEntity.ok(tourService.updateTour(id, request, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User '{}' deleting tour id={}", username, id);
        tourService.deleteTour(id, username);
        return ResponseEntity.noContent().build();
    }
}
