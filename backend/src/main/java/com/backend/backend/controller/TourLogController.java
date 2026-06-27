package com.backend.backend.controller;

import com.backend.backend.model.dto.TourLogRequest;
import com.backend.backend.model.dto.TourLogResponse;
import com.backend.backend.service.declaration.ITourLogService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Logs are nested under a tour because they only exist in the context of one tour.
// URL pattern:  /api/tours/{tourId}/logs       — for create + list
//               /api/tours/{tourId}/logs/{id}  — for update + delete
@Slf4j
@RestController
@RequestMapping("/api/tours/{tourId}/logs")
@CrossOrigin
public class TourLogController {

    private final ITourLogService tourLogService;

    public TourLogController(ITourLogService tourLogService) {
        this.tourLogService = tourLogService;
    }

    @PostMapping
    public ResponseEntity<TourLogResponse> create(@PathVariable int tourId,
                                                  @Valid @RequestBody TourLogRequest request) {
        // SecurityContextHolder is how Spring tells us which user is logged in.
        // The JwtAuthFilter puts the username here after verifying the token.
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        TourLogResponse created = tourLogService.createLog(tourId, request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<TourLogResponse>> getAll(@PathVariable int tourId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(tourLogService.getLogsForTour(tourId, username));
    }

    @PutMapping("/{logId}")
    public ResponseEntity<TourLogResponse> update(@PathVariable int tourId,
                                                  @PathVariable int logId,
                                                  @Valid @RequestBody TourLogRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(tourLogService.updateLog(tourId, logId, request, username));
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> delete(@PathVariable int tourId, @PathVariable int logId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        tourLogService.deleteLog(tourId, logId, username);
        return ResponseEntity.noContent().build();
    }
}
