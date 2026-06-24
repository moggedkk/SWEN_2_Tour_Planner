package com.backend.backend.service.implementation;

import com.backend.backend.exception.TourNotFoundException;
import com.backend.backend.model.dto.RouteResult;
import com.backend.backend.model.dto.TourRequest;
import com.backend.backend.model.dto.TourResponse;
import com.backend.backend.model.entity.Difficulty;
import com.backend.backend.model.entity.Tour;
import com.backend.backend.model.entity.TourLog;
import com.backend.backend.model.entity.TourTransportType;
import com.backend.backend.model.entity.User;
import com.backend.backend.repository.DifficultyRepository;
import com.backend.backend.repository.TourLogRepository;
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

import java.util.ArrayList;
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
    private final TourLogRepository tourLogRepository;
    private final TourAttributeCalculator attributeCalculator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TourServiceImpl(TourRepository tourRepository,
                           DifficultyRepository difficultyRepository,
                           TourTransportTypeRepository transportTypeRepository,
                           IUserService userService,
                           IOpenRouteService openRouteService,
                           ITourLogService tourLogService,
                           TourLogRepository tourLogRepository,
                           TourAttributeCalculator attributeCalculator) {
        this.tourRepository = tourRepository;
        this.difficultyRepository = difficultyRepository;
        this.transportTypeRepository = transportTypeRepository;
        this.userService = userService;
        this.openRouteService = openRouteService;
        this.tourLogService = tourLogService;
        this.tourLogRepository = tourLogRepository;
        this.attributeCalculator = attributeCalculator;
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
    @Transactional
    public List<TourResponse> importTours(List<TourRequest> requests, String username) {
        log.info("User '{}' bulk-importing {} tour(s)", username, requests.size());

        List<TourResponse> imported = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            TourRequest req = requests.get(i);
            try {
                // we're already inside this method's @Transactional, so the
                // inner createTour call joins the same transaction (Spring's
                // default REQUIRED propagation). that's exactly what we want:
                // any failure here throws, the whole batch rolls back.
                imported.add(createTour(req, username));
            } catch (RuntimeException e) {
                // wrap so the user knows WHICH tour broke the import. then
                // rethrow so @Transactional rolls everything back.
                String name = req != null ? req.getName() : null;
                log.warn("Import aborted at tour idx={} name='{}': {}", i, name, e.getMessage());
                throw new RuntimeException(
                        "Import failed at tour #" + (i + 1) + " ('" + name + "'): " + e.getMessage(), e);
            }
        }

        log.info("Imported {} tour(s) for user '{}'", imported.size(), username);
        return imported;
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
    public List<TourResponse> searchTours(String start, String end, String transport, String query, String username) {
        log.debug("User '{}' searching tours start='{}' end='{}' transport='{}' q='{}'",
                username, start, end, transport, query);
        User user = userService.findUserByUsername(username).orElseThrow();
        List<Tour> allTours = tourRepository.findByUser(user);

        // normalize all filters once — null/blank means "ignore this filter"
        String startNeedle = normalize(start);
        String endNeedle = normalize(end);
        String transportNeedle = normalize(transport);
        String queryNeedle = normalize(query);

        // no filters at all -> behave like getAllTours
        if (startNeedle == null && endNeedle == null && transportNeedle == null && queryNeedle == null) {
            return allTours.stream().map(this::toResponse).toList();
        }

        return allTours.stream()
                .filter(tour -> matchesField(tour.getStartLocation(), startNeedle))
                .filter(tour -> matchesField(tour.getEndLocation(), endNeedle))
                .filter(tour -> matchesTransport(tour, transportNeedle))
                .filter(tour -> matchesFullText(tour, queryNeedle))
                .map(this::toResponse)
                .toList();
    }

    // Lowercase + trim, but return null for blank so we can treat "no filter" uniformly.
    private String normalize(String value) {
        if (value == null || value.isBlank()) return null;
        return value.toLowerCase().trim();
    }

    // Single-field substring match. If the needle is null (no filter) it always passes.
    private boolean matchesField(String fieldValue, String needle) {
        if (needle == null) return true;
        if (fieldValue == null) return false;
        return fieldValue.toLowerCase().contains(needle);
    }

    private boolean matchesTransport(Tour tour, String needle) {
        if (needle == null) return true;
        if (tour.getTransportType() == null) return false;
        return matchesField(tour.getTransportType().getTransportTypeValue(), needle);
    }

    // Full-text — checks the big concatenated string of tour fields + logs + computed attrs.
    private boolean matchesFullText(Tour tour, String needle) {
        if (needle == null) return true;
        return buildSearchableText(tour).toLowerCase().contains(needle);
    }

    // Concatenates everything we want to be searchable into a single string.
    // Pull out into its own method so the search rule is easy to read and tweak.
    private String buildSearchableText(Tour tour) {
        List<TourLog> logs = tourLogRepository.findByTour(tour);
        StringBuilder sb = new StringBuilder();

        // ---- tour fields ----
        appendIfNotBlank(sb, tour.getTourName());
        appendIfNotBlank(sb, tour.getDescription());
        appendIfNotBlank(sb, tour.getStartLocation());
        appendIfNotBlank(sb, tour.getEndLocation());
        if (tour.getDifficulty() != null) {
            appendIfNotBlank(sb, tour.getDifficulty().getDifficultyValue());
        }
        if (tour.getTransportType() != null) {
            appendIfNotBlank(sb, tour.getTransportType().getTransportTypeValue());
        }

        // ---- every log belonging to this tour ----
        for (TourLog tourLog : logs) {
            appendIfNotBlank(sb, tourLog.getComment());
            appendIfNotBlank(sb, tourLog.getDifficulty());
            if (tourLog.getDateTime() != null) sb.append(' ').append(tourLog.getDateTime());
            sb.append(' ').append(tourLog.getTotalDistance());
            sb.append(' ').append(tourLog.getTotalTime());
            sb.append(' ').append(tourLog.getRating());
        }

        // ---- computed attributes (spec wants these to count too) ----
        sb.append(' ').append(attributeCalculator.computePopularity(logs.size()));
        sb.append(' ').append(attributeCalculator.computeChildFriendliness(logs));

        return sb.toString();
    }

    private void appendIfNotBlank(StringBuilder sb, String value) {
        if (value != null && !value.isBlank()) {
            sb.append(' ').append(value);
        }
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

        // Pull all logs for this tour so we can compute the "automatic" attributes the spec wants.
        // Note: this is one extra query per tour. Fine for now — if the tour list ever gets really big
        // we'd want to fetch all logs once and group them in memory, but that's a later optimization.
        List<TourLog> logs = tourLogRepository.findByTour(tour);
        String popularity = attributeCalculator.computePopularity(logs.size());
        String childFriendliness = attributeCalculator.computeChildFriendliness(logs);

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
                routeGeometry,
                popularity,
                childFriendliness
        );
    }
}
