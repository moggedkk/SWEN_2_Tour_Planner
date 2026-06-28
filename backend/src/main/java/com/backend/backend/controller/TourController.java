package com.backend.backend.controller;

import com.backend.backend.model.dto.TourRequest;
import com.backend.backend.model.dto.TourResponse;
import com.backend.backend.service.declaration.ITourService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @Validated enables Spring's method validation, which is required for
// container element validation (List<@Valid TourRequest>) in /import.
@Slf4j
@RestController
@Validated
@RequestMapping("/api/tours")
@CrossOrigin
public class TourController {

    private final ITourService tourService;

    public TourController(ITourService tourService) {
        this.tourService = tourService;
    }

    @PostMapping
    public ResponseEntity<TourResponse> create(@Valid @RequestBody TourRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User '{}' creating tour '{}'", username, request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(tourService.createTour(request, username));
    }

    // bulk import. all-or-nothing: a single bad tour rolls back the whole batch
    // and the user gets a 500 with the failing tour's index + reason. on success
    // we return 201 with the full list of created tours.
    // @Valid on the element type so each TourRequest in the list gets checked.
    // A single bad entry fails the whole batch with a 400 + field errors.
    @PostMapping("/import")
    public ResponseEntity<List<TourResponse>> importTours(@RequestBody List<@Valid TourRequest> requests) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User '{}' importing {} tour(s)", username, requests.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(tourService.importTours(requests, username));
    }

    // export. returns the user's tours in the SAME shape that /import accepts,
    // so a user can export, edit the file, re-import without translation.
    @GetMapping("/export")
    public ResponseEntity<List<TourRequest>> exportTours() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User '{}' exporting tours", username);
        return ResponseEntity.ok(tourService.exportTours(username));
    }

    @GetMapping
    public ResponseEntity<List<TourResponse>> getAll() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("User '{}' fetching all tours", username);
        return ResponseEntity.ok(tourService.getAllTours(username));
    }

    // Multi-field search. All params optional — missing ones simply aren't applied.
    // start/end/transport filter on the tour's own fields; q is a full-text search across
    // tour fields + logs + computed attributes (popularity, child-friendliness).
    // All four are AND-combined, so a tour must match every supplied filter.
    @GetMapping("/search")
    public ResponseEntity<List<TourResponse>> search(
            @RequestParam(value = "start", defaultValue = "") String start,
            @RequestParam(value = "end", defaultValue = "") String end,
            @RequestParam(value = "transport", defaultValue = "") String transport,
            @RequestParam(value = "q", defaultValue = "") String query) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("User '{}' searching tours start='{}' end='{}' transport='{}' q='{}'",
                username, start, end, transport, query);
        return ResponseEntity.ok(tourService.searchTours(start, end, transport, query, username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourResponse> getOne(@PathVariable int id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("User '{}' fetching tour id={}", username, id);
        return ResponseEntity.ok(tourService.getTour(id, username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourResponse> update(@PathVariable int id, @Valid @RequestBody TourRequest request) {
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
