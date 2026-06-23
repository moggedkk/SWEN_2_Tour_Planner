package com.backend.backend.service;

import com.backend.backend.exception.TourNotFoundException;
import com.backend.backend.model.dto.RouteResult;
import com.backend.backend.model.dto.TourRequest;
import com.backend.backend.model.dto.TourResponse;
import com.backend.backend.model.entity.Difficulty;
import com.backend.backend.model.entity.Tour;
import com.backend.backend.model.entity.TourTransportType;
import com.backend.backend.model.entity.User;
import com.backend.backend.repository.DifficultyRepository;
import com.backend.backend.repository.TourLogRepository;
import com.backend.backend.repository.TourRepository;
import com.backend.backend.repository.TourTransportTypeRepository;
import com.backend.backend.service.declaration.IOpenRouteService;
import com.backend.backend.service.declaration.ITourLogService;
import com.backend.backend.service.declaration.IUserService;
import com.backend.backend.service.implementation.TourAttributeCalculator;
import com.backend.backend.service.implementation.TourServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourServiceImplTest {

    @Mock private TourRepository tourRepository;
    @Mock private DifficultyRepository difficultyRepository;
    @Mock private TourTransportTypeRepository transportTypeRepository;
    @Mock private IUserService userService;
    @Mock private IOpenRouteService openRouteService;
    @Mock private ITourLogService tourLogService;
    @Mock private TourLogRepository tourLogRepository;
    @Mock private TourAttributeCalculator attributeCalculator;

    @InjectMocks
    private TourServiceImpl tourService;

    private User user;
    private Tour tour;
    private TourRequest request;
    private RouteResult routeResult;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");

        Difficulty difficulty = new Difficulty();
        difficulty.setDifficultyValue("easy");

        TourTransportType transportType = new TourTransportType();
        transportType.setTransportTypeValue("foot-walking");

        tour = new Tour();
        tour.setId(1);
        tour.setTourName("Test Tour");
        tour.setStartLocation("Vienna");
        tour.setEndLocation("Graz");
        tour.setDescription("A nice tour");
        tour.setDistance(200_000);
        tour.setEstimatedTime(7200);
        tour.setDifficulty(difficulty);
        tour.setTransportType(transportType);
        tour.setUser(user);

        request = new TourRequest("Test Tour", "Vienna", "Graz", "A nice tour", "easy", "foot-walking");
        routeResult = new RouteResult(200_000, 7200, "{\"type\":\"FeatureCollection\"}");
    }

    @Test
    void createTour_success_returnsMappedResponse() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(openRouteService.getRoute("Vienna", "Graz", "foot-walking")).thenReturn(routeResult);
        when(tourRepository.save(any())).thenReturn(tour);
        when(difficultyRepository.save(any())).thenReturn(tour.getDifficulty());
        when(transportTypeRepository.save(any())).thenReturn(tour.getTransportType());

        TourResponse response = tourService.createTour(request, "testuser");

        assertThat(response.getName()).isEqualTo("Test Tour");
        assertThat(response.getStart()).isEqualTo("Vienna");
        assertThat(response.getEnd()).isEqualTo("Graz");
        assertThat(response.getDistance()).isEqualTo(200_000);
        verify(tourRepository, times(2)).save(any());
    }

    @Test
    void createTour_userNotFound_throwsNoSuchElement() {
        when(userService.findUserByUsername("ghost")).thenReturn(Optional.empty());

        // orElseThrow() on an empty Optional throws NoSuchElementException
        assertThatThrownBy(() -> tourService.createTour(request, "ghost"))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    void getTour_success_returnsTourResponse() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(tour));

        TourResponse response = tourService.getTour(1, "testuser");

        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getName()).isEqualTo("Test Tour");
    }

    @Test
    void getTour_notFound_throwsTourNotFoundException() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByIdAndUser(99, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tourService.getTour(99, "testuser"))
                .isInstanceOf(TourNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllTours_returnsMappedListForUser() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByUser(user)).thenReturn(List.of(tour));

        List<TourResponse> result = tourService.getAllTours("testuser");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Tour");
    }

    @Test
    void getAllTours_noToursForUser_returnsEmptyList() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByUser(user)).thenReturn(List.of());

        List<TourResponse> result = tourService.getAllTours("testuser");

        assertThat(result).isEmpty();
    }

    @Test
    void updateTour_success_updatesFieldsAndReturnsResponse() {
        TourRequest updateRequest = new TourRequest("Updated Tour", "Linz", "Salzburg", "Updated", "hard", "driving-car");
        RouteResult updatedRoute = new RouteResult(130_000, 5400, "{\"type\":\"FeatureCollection\"}");

        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(tour));
        when(openRouteService.getRoute("Linz", "Salzburg", "driving-car")).thenReturn(updatedRoute);
        when(tourRepository.save(any())).thenReturn(tour);

        TourResponse response = tourService.updateTour(1, updateRequest, "testuser");

        verify(difficultyRepository).save(any());
        verify(transportTypeRepository).save(any());
        assertThat(response).isNotNull();
    }

    @Test
    void deleteTour_success_deletesTourAndRelatedEntities() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(tour));

        // Capture references before calling the service — deleteTour sets them to null
        // on the tour object during cleanup, so tour.getDifficulty() would return null
        // by the time verify() runs
        Difficulty expectedDifficulty = tour.getDifficulty();
        TourTransportType expectedTransportType = tour.getTransportType();

        tourService.deleteTour(1, "testuser");

        verify(difficultyRepository).delete(expectedDifficulty);
        verify(transportTypeRepository).delete(expectedTransportType);
        verify(tourRepository).delete(tour);
    }

    @Test
    void deleteTour_notFound_throwsTourNotFoundException() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByIdAndUser(99, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tourService.deleteTour(99, "testuser"))
                .isInstanceOf(TourNotFoundException.class);
    }

    // ---- multi-field search ----

    @Test
    void searchTours_allFiltersEmpty_returnsAllTours() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByUser(user)).thenReturn(List.of(tour));
        when(tourLogRepository.findByTour(tour)).thenReturn(List.of());
        when(attributeCalculator.computePopularity(0)).thenReturn("Low");
        when(attributeCalculator.computeChildFriendliness(List.of())).thenReturn("Low");

        // every filter empty -> same as getAllTours
        List<TourResponse> result = tourService.searchTours("", "", "", "", "testuser");

        assertThat(result).hasSize(1);
    }

    @Test
    void searchTours_fullTextMatchesTourName_returnsTour() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByUser(user)).thenReturn(List.of(tour));
        when(tourLogRepository.findByTour(tour)).thenReturn(List.of());
        when(attributeCalculator.computePopularity(0)).thenReturn("Low");
        when(attributeCalculator.computeChildFriendliness(List.of())).thenReturn("Low");

        // tour.name = "Test Tour" — searching for "test" should match
        List<TourResponse> result = tourService.searchTours("", "", "", "test", "testuser");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Tour");
    }

    @Test
    void searchTours_isCaseInsensitive() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByUser(user)).thenReturn(List.of(tour));
        when(tourLogRepository.findByTour(tour)).thenReturn(List.of());
        when(attributeCalculator.computePopularity(0)).thenReturn("Low");
        when(attributeCalculator.computeChildFriendliness(List.of())).thenReturn("Low");

        // matches startLocation "Vienna" via the full-text query
        List<TourResponse> result = tourService.searchTours("", "", "", "VIENNA", "testuser");

        assertThat(result).hasSize(1);
    }

    @Test
    void searchTours_fullTextMatchesLogComment_returnsTour() {
        com.backend.backend.model.entity.TourLog logEntry = new com.backend.backend.model.entity.TourLog();
        logEntry.setComment("Beautiful weather");
        logEntry.setTour(tour);

        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByUser(user)).thenReturn(List.of(tour));
        when(tourLogRepository.findByTour(tour)).thenReturn(List.of(logEntry));
        when(attributeCalculator.computePopularity(1)).thenReturn("Medium");
        when(attributeCalculator.computeChildFriendliness(List.of(logEntry))).thenReturn("Low");

        // search term is in a LOG's comment, not in any tour field
        List<TourResponse> result = tourService.searchTours("", "", "", "weather", "testuser");

        assertThat(result).hasSize(1);
    }

    @Test
    void searchTours_fullTextMatchesComputedPopularity_returnsTour() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByUser(user)).thenReturn(List.of(tour));
        when(tourLogRepository.findByTour(tour)).thenReturn(List.of());
        when(attributeCalculator.computePopularity(0)).thenReturn("High");
        when(attributeCalculator.computeChildFriendliness(List.of())).thenReturn("Low");

        // spec says computed values must also be searchable
        List<TourResponse> result = tourService.searchTours("", "", "", "high", "testuser");

        assertThat(result).hasSize(1);
    }

    @Test
    void searchTours_noMatch_returnsEmptyList() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByUser(user)).thenReturn(List.of(tour));
        when(tourLogRepository.findByTour(tour)).thenReturn(List.of());
        when(attributeCalculator.computePopularity(0)).thenReturn("Low");
        when(attributeCalculator.computeChildFriendliness(List.of())).thenReturn("Low");

        List<TourResponse> result = tourService.searchTours("", "", "", "nonexistent-gibberish", "testuser");

        assertThat(result).isEmpty();
    }

    @Test
    void searchTours_startFilter_matchesTour() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByUser(user)).thenReturn(List.of(tour));
        when(tourLogRepository.findByTour(tour)).thenReturn(List.of());
        when(attributeCalculator.computePopularity(0)).thenReturn("Low");
        when(attributeCalculator.computeChildFriendliness(List.of())).thenReturn("Low");

        // tour.startLocation = "Vienna" -> "vien" should match
        List<TourResponse> result = tourService.searchTours("vien", "", "", "", "testuser");

        assertThat(result).hasSize(1);
    }

    @Test
    void searchTours_startFilter_doesNotMatch_returnsEmpty() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByUser(user)).thenReturn(List.of(tour));

        // start filter doesn't match -> tour gets filtered out before we even need
        // the log lookup or computed attrs, so those mocks aren't required here
        List<TourResponse> result = tourService.searchTours("Berlin", "", "", "", "testuser");

        assertThat(result).isEmpty();
    }

    @Test
    void searchTours_endFilter_matchesTour() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByUser(user)).thenReturn(List.of(tour));
        when(tourLogRepository.findByTour(tour)).thenReturn(List.of());
        when(attributeCalculator.computePopularity(0)).thenReturn("Low");
        when(attributeCalculator.computeChildFriendliness(List.of())).thenReturn("Low");

        // tour.endLocation = "Graz"
        List<TourResponse> result = tourService.searchTours("", "graz", "", "", "testuser");

        assertThat(result).hasSize(1);
    }

    @Test
    void searchTours_transportFilter_matchesTour() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByUser(user)).thenReturn(List.of(tour));
        when(tourLogRepository.findByTour(tour)).thenReturn(List.of());
        when(attributeCalculator.computePopularity(0)).thenReturn("Low");
        when(attributeCalculator.computeChildFriendliness(List.of())).thenReturn("Low");

        // tour.transportType = "foot-walking"
        List<TourResponse> result = tourService.searchTours("", "", "foot-walking", "", "testuser");

        assertThat(result).hasSize(1);
    }

    @Test
    void searchTours_transportFilter_doesNotMatch_returnsEmpty() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByUser(user)).thenReturn(List.of(tour));

        List<TourResponse> result = tourService.searchTours("", "", "driving-car", "", "testuser");

        assertThat(result).isEmpty();
    }

    @Test
    void searchTours_allFiltersCombined_AND_match() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByUser(user)).thenReturn(List.of(tour));
        when(tourLogRepository.findByTour(tour)).thenReturn(List.of());
        when(attributeCalculator.computePopularity(0)).thenReturn("Low");
        when(attributeCalculator.computeChildFriendliness(List.of())).thenReturn("Low");

        // every filter matches the one tour we have
        List<TourResponse> result = tourService.searchTours("vienna", "graz", "foot-walking", "test", "testuser");

        assertThat(result).hasSize(1);
    }

    @Test
    void searchTours_allFiltersCombined_oneFails_AND_excludes() {
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourRepository.findByUser(user)).thenReturn(List.of(tour));

        // start/end/transport all match, but the full-text query doesn't ->
        // AND semantics means the tour is filtered out
        List<TourResponse> result = tourService.searchTours("vienna", "graz", "foot-walking", "nonexistent", "testuser");

        assertThat(result).isEmpty();
    }
}
