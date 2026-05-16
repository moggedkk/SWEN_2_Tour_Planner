package com.backend.backend.service.implementation;

import com.backend.backend.exception.TourNotFoundException;
import com.backend.backend.model.dto.TourRequest;
import com.backend.backend.model.dto.TourResponse;
import com.backend.backend.model.entity.Difficulty;
import com.backend.backend.model.entity.Tour;
import com.backend.backend.model.entity.TourTransportType;
import com.backend.backend.model.entity.User;
import com.backend.backend.repository.DifficultyRepository;
import com.backend.backend.repository.TourRepository;
import com.backend.backend.repository.TourTransportTypeRepository;
import com.backend.backend.service.declaration.ITourService;
import com.backend.backend.service.declaration.IUserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TourServiceImpl implements ITourService {

    private final TourRepository tourRepository;
    private final DifficultyRepository difficultyRepository;
    private final TourTransportTypeRepository transportTypeRepository;
    private final IUserService userService;

    public TourServiceImpl(TourRepository tourRepository,
                           DifficultyRepository difficultyRepository,
                           TourTransportTypeRepository transportTypeRepository,
                           IUserService userService) {
        this.tourRepository = tourRepository;
        this.difficultyRepository = difficultyRepository;
        this.transportTypeRepository = transportTypeRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public TourResponse createTour(TourRequest request, String username) {
        User user = userService.findUserByUsername(username).orElseThrow();

        Tour tour = new Tour();
        tour.setTourName(request.getName());
        tour.setStartLocation(request.getStart());
        tour.setEndLocation(request.getEnd());
        tour.setDescription(request.getDescription());
        tour.setDistance(request.getDistance());
        tour.setEstimatedTime(request.getEstimatedTime());
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
        return toResponse(tourRepository.save(saved));
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
        User user = userService.findUserByUsername(username).orElseThrow();
        Tour tour = tourRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new TourNotFoundException(id));

        tour.setTourName(request.getName());
        tour.setStartLocation(request.getStart());
        tour.setEndLocation(request.getEnd());
        tour.setDescription(request.getDescription());
        tour.setDistance(request.getDistance());
        tour.setEstimatedTime(request.getEstimatedTime());

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
        User user = userService.findUserByUsername(username).orElseThrow();
        Tour tour = tourRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new TourNotFoundException(id));

        Difficulty difficulty = tour.getDifficulty();
        TourTransportType transportType = tour.getTransportType();

        // Null out the FKs first so the child rows can be deleted without constraint violation
        tour.setDifficulty(null);
        tour.setTransportType(null);
        tourRepository.save(tour);

        if (difficulty != null) difficultyRepository.delete(difficulty);
        if (transportType != null) transportTypeRepository.delete(transportType);

        tourRepository.delete(tour);
    }

    private TourResponse toResponse(Tour tour) {
        String difficultyValue = tour.getDifficulty() != null ? tour.getDifficulty().getDifficultyValue() : null;
        String transportTypeValue = tour.getTransportType() != null ? tour.getTransportType().getTransportTypeValue() : null;
        return new TourResponse(
                tour.getId(),
                tour.getTourName(),
                tour.getStartLocation(),
                tour.getEndLocation(),
                tour.getDescription(),
                difficultyValue,
                transportTypeValue,
                tour.getDistance(),
                tour.getEstimatedTime()
        );
    }
}
