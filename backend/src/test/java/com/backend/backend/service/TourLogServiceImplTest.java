package com.backend.backend.service;

import com.backend.backend.exception.TourLogNotFoundException;
import com.backend.backend.exception.TourNotFoundException;
import com.backend.backend.model.dto.TourLogRequest;
import com.backend.backend.model.dto.TourLogResponse;
import com.backend.backend.model.entity.Tour;
import com.backend.backend.model.entity.TourLog;
import com.backend.backend.model.entity.User;
import com.backend.backend.repository.TourLogRepository;
import com.backend.backend.repository.TourRepository;
import com.backend.backend.service.declaration.IUserService;
import com.backend.backend.service.implementation.TourLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourLogServiceImplTest {

    @Mock private TourLogRepository tourLogRepository;
    @Mock private TourRepository tourRepository;
    @Mock private IUserService userService;

    @InjectMocks
    private TourLogServiceImpl tourLogService;

    private User user;
    private Tour tour;
    private TourLogRequest request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");

        tour = new Tour();
        tour.setId(1);
        tour.setTourName("Test Tour");
        tour.setUser(user);

        request = new TourLogRequest(
                LocalDateTime.of(2024, 1, 15, 10, 30),
                "Great hike",
                "easy",
                12.5,
                90.0,
                5,
                null,
                null
        );
    }

    @Test
    void createLog_success_returnsResponseWithCorrectFields() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(tour));

        // The save will return a log with id=42 set on it (simulating the DB assigning an id)
        when(tourLogRepository.save(any(TourLog.class))).thenAnswer(call -> {
            TourLog saved = call.getArgument(0);
            saved.setId(42);
            return saved;
        });

        TourLogResponse response = tourLogService.createLog(1, request, "testuser");

        assertThat(response.getId()).isEqualTo(42);
        assertThat(response.getTourId()).isEqualTo(1);
        assertThat(response.getComment()).isEqualTo("Great hike");
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getTotalDistance()).isEqualTo(12.5);
    }

    @Test
    void createLog_tourNotFound_throwsTourNotFoundException() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByIdAndUser(99, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tourLogService.createLog(99, request, "testuser"))
                .isInstanceOf(TourNotFoundException.class);
    }

    @Test
    void getLogsForTour_returnsListMappedToResponses() {
        TourLog log1 = new TourLog();
        log1.setId(1);
        log1.setTour(tour);
        log1.setComment("first log");

        TourLog log2 = new TourLog();
        log2.setId(2);
        log2.setTour(tour);
        log2.setComment("second log");

        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findByTour(tour)).thenReturn(List.of(log1, log2));

        List<TourLogResponse> result = tourLogService.getLogsForTour(1, "testuser");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getComment()).isEqualTo("first log");
        assertThat(result.get(1).getComment()).isEqualTo("second log");
    }

    @Test
    void updateLog_success_updatesFieldsAndReturnsResponse() {
        TourLog existing = new TourLog();
        existing.setId(5);
        existing.setTour(tour);
        existing.setComment("old comment");

        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findById(5)).thenReturn(Optional.of(existing));
        when(tourLogRepository.save(any(TourLog.class))).thenAnswer(call -> call.getArgument(0));

        TourLogResponse response = tourLogService.updateLog(1, 5, request, "testuser");

        assertThat(response.getComment()).isEqualTo("Great hike");
        assertThat(response.getRating()).isEqualTo(5);
    }

    @Test
    void updateLog_logBelongsToDifferentTour_throwsTourLogNotFound() {
        Tour someOtherTour = new Tour();
        someOtherTour.setId(999);

        TourLog logFromOtherTour = new TourLog();
        logFromOtherTour.setId(5);
        logFromOtherTour.setTour(someOtherTour); // log belongs to a different tour!

        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findById(5)).thenReturn(Optional.of(logFromOtherTour));

        // even though the log exists, it doesn't belong to tour 1 — should reject
        assertThatThrownBy(() -> tourLogService.updateLog(1, 5, request, "testuser"))
                .isInstanceOf(TourLogNotFoundException.class);
    }

    @Test
    void deleteLog_success_deletesFromRepository() {
        TourLog existing = new TourLog();
        existing.setId(5);
        existing.setTour(tour);

        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findById(5)).thenReturn(Optional.of(existing));

        tourLogService.deleteLog(1, 5, "testuser");

        verify(tourLogRepository).delete(existing);
    }

    @Test
    void deleteLog_logNotFound_throwsTourLogNotFoundException() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tourLogService.deleteLog(1, 99, "testuser"))
                .isInstanceOf(TourLogNotFoundException.class);
    }

    @Test
    void deleteAllLogsForTour_callsRepositoryDeleteByTour() {
        tourLogService.deleteAllLogsForTour(tour);

        verify(tourLogRepository).deleteByTour(tour);
    }
}
