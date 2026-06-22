package com.backend.backend.service.implementation;

import com.backend.backend.exception.TourLogNotFoundException;
import com.backend.backend.exception.TourNotFoundException;
import com.backend.backend.model.dto.TourLogRequest;
import com.backend.backend.model.dto.TourLogResponse;
import com.backend.backend.model.entity.Tour;
import com.backend.backend.model.entity.TourLog;
import com.backend.backend.model.entity.User;
import com.backend.backend.repository.TourLogRepository;
import com.backend.backend.repository.TourRepository;
import com.backend.backend.service.declaration.ITourLogService;
import com.backend.backend.service.declaration.IUserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TourLogServiceImpl implements ITourLogService {

    private final TourLogRepository tourLogRepository;
    private final TourRepository tourRepository;
    private final IUserService userService;

    public TourLogServiceImpl(TourLogRepository tourLogRepository,
                              TourRepository tourRepository,
                              IUserService userService) {
        this.tourLogRepository = tourLogRepository;
        this.tourRepository = tourRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public TourLogResponse createLog(int tourId, TourLogRequest request, String username) {
        log.info("User '{}' creating a log for tour id={}", username, tourId);

        // Step 1: find the tour AND check the user owns it.
        // findTourOwnedBy throws if either the tour doesn't exist or it's someone else's tour.
        Tour tour = findTourOwnedBy(tourId, username);

        // Step 2: build a new TourLog from the request fields
        TourLog newLog = new TourLog();
        newLog.setTour(tour);
        copyRequestIntoLog(request, newLog);

        // Step 3: save and return as a response DTO
        TourLog saved = tourLogRepository.save(newLog);
        log.info("Log id={} saved for tour id={}", saved.getId(), tourId);
        return toResponse(saved);
    }

    @Override
    public List<TourLogResponse> getLogsForTour(int tourId, String username) {
        log.debug("User '{}' fetching logs for tour id={}", username, tourId);
        Tour tour = findTourOwnedBy(tourId, username);

        return tourLogRepository.findByTour(tour).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TourLogResponse updateLog(int tourId, int logId, TourLogRequest request, String username) {
        log.info("User '{}' updating log id={} for tour id={}", username, logId, tourId);
        Tour tour = findTourOwnedBy(tourId, username);

        // grab the existing log, throw 404 if it's not there
        TourLog existing = tourLogRepository.findById(logId)
                .orElseThrow(() -> new TourLogNotFoundException(logId));

        // make sure this log actually belongs to the tour from the URL
        // (prevents someone from updating a log that belongs to a different tour)
        if (existing.getTour().getId() != tour.getId()) {
            throw new TourLogNotFoundException(logId);
        }

        copyRequestIntoLog(request, existing);
        return toResponse(tourLogRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteLog(int tourId, int logId, String username) {
        log.info("User '{}' deleting log id={} for tour id={}", username, logId, tourId);
        Tour tour = findTourOwnedBy(tourId, username);

        TourLog existing = tourLogRepository.findById(logId)
                .orElseThrow(() -> new TourLogNotFoundException(logId));

        if (existing.getTour().getId() != tour.getId()) {
            throw new TourLogNotFoundException(logId);
        }

        tourLogRepository.delete(existing);
    }

    @Override
    @Transactional
    public void deleteAllLogsForTour(Tour tour) {
        log.debug("Deleting all logs for tour id={}", tour.getId());
        tourLogRepository.deleteByTour(tour);
    }

    // ----- helpers below -----

    // Finds a tour by id AND verifies that the logged-in user owns it.
    // Used by every public method here, so we don't repeat the check 4 times.
    private Tour findTourOwnedBy(int tourId, String username) {
        User user = userService.findUserByUsername(username).orElseThrow();
        return tourRepository.findByIdAndUser(tourId, user)
                .orElseThrow(() -> new TourNotFoundException(tourId));
    }

    // Copies the fields from the request DTO into a TourLog entity.
    // Pulled out so create and update don't duplicate the same 6 setters.
    // Note: the parameter is called "target" instead of "log" because @Slf4j already gives us
    // a logger named "log" in this class — naming the param "log" would shadow it.
    private void copyRequestIntoLog(TourLogRequest request, TourLog target) {
        target.setDateTime(request.getDateTime());
        target.setComment(request.getComment());
        target.setDifficulty(request.getDifficulty());
        target.setTotalDistance(request.getTotalDistance());
        target.setTotalTime(request.getTotalTime());
        target.setRating(request.getRating());
    }

    // Converts a TourLog entity to a TourLogResponse DTO.
    // We don't want to expose the full Tour object in the response — just the id is enough.
    private TourLogResponse toResponse(TourLog tourLog) {
        return new TourLogResponse(
                tourLog.getId(),
                tourLog.getTour().getId(),
                tourLog.getDateTime(),
                tourLog.getComment(),
                tourLog.getDifficulty(),
                tourLog.getTotalDistance(),
                tourLog.getTotalTime(),
                tourLog.getRating()
        );
    }
}
