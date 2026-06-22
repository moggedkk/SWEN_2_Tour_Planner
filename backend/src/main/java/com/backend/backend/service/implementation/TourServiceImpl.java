package com.backend.backend.service.implementation;

import com.backend.backend.exception.TourNotFoundException;
import com.backend.backend.model.dto.RouteResult;
import com.backend.backend.model.dto.TourRequest;
import com.backend.backend.model.dto.TourResponse;
import com.backend.backend.model.entity.Difficulty;
import com.backend.backend.model.entity.Tour;
import com.backend.backend.model.entity.TourTransportType;
import com.backend.backend.model.entity.User;
import com.backend.backend.repository.DifficultyRepository;
import com.backend.backend.repository.TourRepository;
import com.backend.backend.repository.TourTransportTypeRepository;
import com.backend.backend.service.declaration.IOpenRouteService;
import com.backend.backend.service.declaration.ITourLogService;
import com.backend.backend.service.declaration.ITourService;
import com.backend.backend.service.declaration.IUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TourServiceImpl implements ITourService {

    private final TourRepository tourRepository;
    private final DifficultyRepository difficultyRepository;
    private final TourTransportTypeRepository transportTypeRepository;
    private final IUserService userService;
    private final IOpenRouteService openRouteService;
    private final ITourLogService tourLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TourServiceImpl(TourRepository tourRepository,
                           DifficultyRepository difficultyRepository,
                           TourTransportTypeRepository transportTypeRepository,
                           IUserService userService,
                           IOpenRouteService openRouteService,
                           ITourLogService tourLogService) {
        this.tourRepository = tourRepository;
        this.difficultyRepository = difficultyRepository;
        this.transportTypeRepository = transportTypeRepository;
        this.userService = userService;
        this.openRouteService = openRouteService;
        this.tourLogService = tourLogService;
    }

    @Override
    @Transactional
    public TourResponse createTour(TourRequest request, String username) {
        log.info("Creating tour '{}' for user '{}'", request.getName(), username);
        User user = userService.findUserByUsername(username).orElseThrow();

        log.debug("Fetching route from '{}' to '{}' via '{}'", request.getStart(), request.getEnd(), request.getTransportType());
        RouteResult route = openRouteService.getRoute(request.getStart(), request.getEnd(), request.getTransportType());

        Tour tour = new Tour();
        tour.setTourName(request.getName());
        tour.setStartLocation(request.getStart());
        tour.setEndLocation(request.getEnd());
        tour.setDescription(request.getDescription());
        tour.setDistance(route.distance());
        tour.setEstimatedTime(route.estimatedTime());
        tour.setRouteGeometry(route.routeGeometryJson());
        tour.setUser(user);
        Tour saved = tourRepository.save(tour);

        Difficulty difficulty = new Difficulty();
        difficulty.setDifficultyValue(request.getDifficulty());
        difficulty.setTourId(saved.getId());
        Difficulty savedDifficulty = difficultyRepository.save(difficulty);

        TourTransportType transportType = new TourTransportType();
        transportType.setTransportTypeValue(request.getTransportType());
        transportType.setTourId(saved.getId());
        TourTransportType savedTransportType = transportTypeRepository.save(transportType);

        saved.setDifficulty(savedDifficulty);
        saved.setTransportType(savedTransportType);
        TourResponse response = toResponse(tourRepository.save(saved));
        log.info("Tour '{}' created with id={}", response.getName(), response.getId());
        return response;
    }

    @Override
    public TourResponse getTour(int id, String username) {
        User user = userService.findUserByUsername(username).orElseThrow();
        Tour tour = tourRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new TourNotFoundException(id));
        return toResponse(tour);
    }

    @Override
    public List<TourResponse> getAllTours(String username) {
        User user = userService.findUserByUsername(username).orElseThrow();
        return tourRepository.findByUser(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TourResponse updateTour(int id, TourRequest request, String username) {
        log.info("Updating tour id={} for user '{}'", id, username);
        User user = userService.findUserByUsername(username).orElseThrow();
        Tour tour = tourRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new TourNotFoundException(id));

        RouteResult route = openRouteService.getRoute(request.getStart(), request.getEnd(), request.getTransportType());

        tour.setTourName(request.getName());
        tour.setStartLocation(request.getStart());
        tour.setEndLocation(request.getEnd());
        tour.setDescription(request.getDescription());
        tour.setDistance(route.distance());
        tour.setEstimatedTime(route.estimatedTime());
        tour.setRouteGeometry(route.routeGeometryJson());

        Difficulty difficulty = tour.getDifficulty();
        difficulty.setDifficultyValue(request.getDifficulty());
        difficultyRepository.save(difficulty);

        TourTransportType transportType = tour.getTransportType();
        transportType.setTransportTypeValue(request.getTransportType());
        transportTypeRepository.save(transportType);

        return toResponse(tourRepository.save(tour));
    }

    @Override
    @Transactional
    public void deleteTour(int id, String username) {
        log.info("Deleting tour id={} for user '{}'", id, username);
        User user = userService.findUserByUsername(username).orElseThrow();
        Tour tour = tourRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new TourNotFoundException(id));

        // wipe any logs that belong to this tour first — otherwise the foreign key
        // constraint on tour_logs.tour_id would block the delete
        tourLogService.deleteAllLogsForTour(tour);

        Difficulty difficulty = tour.getDifficulty();
        TourTransportType transportType = tour.getTransportType();

        tour.setDifficulty(null);
        tour.setTransportType(null);
        tourRepository.save(tour);

        if (difficulty != null) difficultyRepository.delete(difficulty);
        if (transportType != null) transportTypeRepository.delete(transportType);

        tourRepository.delete(tour);
        log.info("Tour id={} deleted", id);
    }

    private TourResponse toResponse(Tour tour) {
        String difficultyValue = tour.getDifficulty() != null ? tour.getDifficulty().getDifficultyValue() : null;
        String transportTypeValue = tour.getTransportType() != null ? tour.getTransportType().getTransportTypeValue() : null;

        Object routeGeometry = null;
        if (tour.getRouteGeometry() != null) {
            try {
                routeGeometry = objectMapper.readValue(tour.getRouteGeometry(), Object.class);
            } catch (JsonProcessingException e) {
                log.warn("Malformed route geometry for tour id={}, returning null", tour.getId());
            }
        }

        return new TourResponse(
                tour.getId(),
                tour.getTourName(),
                tour.getStartLocation(),
                tour.getEndLocation(),
                tour.getDescription(),
                difficultyValue,
                transportTypeValue,
                tour.getDistance(),
                tour.getEstimatedTime(),
                routeGeometry
        );
    }
}
