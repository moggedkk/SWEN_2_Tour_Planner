package com.backend.backend.service.implementation;

import com.backend.backend.exception.ImageStorageException;
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
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class TourLogServiceImpl implements ITourLogService {

    private final TourLogRepository tourLogRepository;
    private final TourRepository tourRepository;
    private final IUserService userService;
    @Value("${storage.image-dir}")
    private String baseImagePath;

    // anything not on this list gets rejected before we touch the disk
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    // cap on the decoded image size — base64 in JSON would otherwise let
    // someone post a 100 MB payload and OOM us before Spring can refuse it
    private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;

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
        copyRequestIntoLog(request, newLog, username);

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

        copyRequestIntoLog(request, existing, username);
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
        deleteImage(existing.getFilePath());
    }

    @Override
    @Transactional
    public void deleteAllLogsForTour(Tour tour) {
        log.debug("Deleting all logs for tour id={}", tour.getId());
        tourLogRepository.deleteByTour(tour);
        deleteAllImages();
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
    private void copyRequestIntoLog(TourLogRequest request, TourLog target, String username) {
        target.setDateTime(request.getDateTime());
        target.setComment(request.getComment());
        target.setDifficulty(request.getDifficulty());
        target.setTotalDistance(request.getTotalDistance());
        target.setTotalTime(request.getTotalTime());
        target.setRating(request.getRating());

        // Only write a new file if the user actually attached one. On an update without a
        // new image, the existing filePath / imageName on the entity stay as they were.
        if (request.getImageName() != null && request.getImageEncoded() != null) {
            String storedPath = saveImage(
                    request.getImageEncoded(),
                    request.getImageName(),
                    username,
                    target.getTour().getId()
            );
            target.setFilePath(storedPath);
            target.setImageName(request.getImageName());
        }
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
                tourLog.getRating(),
                tourLog.getImageName(),
                tourLog.getFilePath()
        );
    }
    // Writes the image to <baseImagePath>/<username>/<tourId>/<filename> and returns
    // the relative path we store in the DB. tourId in the path (not tourName) because
    // tour names can contain anything the user typed — including "../" or "/".
    private String saveImage(String imageEncoded, String imageName, String username, int tourId) {
        // tolerate "data:image/...;base64,..." just in case it ever arrives that way
        String base64Data = imageEncoded.contains(",")
                ? imageEncoded.split(",", 2)[1]
                : imageEncoded;

        // getFileName() drops any "../" the client tried to sneak in.
        // Must happen BEFORE the extension check, otherwise the check is bypassable.
        String safeFilename = Paths.get(imageName).getFileName().toString();
        if (safeFilename.isBlank()) {
            throw new ImageStorageException("Image filename is empty");
        }

        int dot = safeFilename.lastIndexOf('.');
        if (dot < 0 || dot == safeFilename.length() - 1) {
            throw new ImageStorageException("Image filename must have an extension");
        }
        String ext = safeFilename.substring(dot + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new ImageStorageException("Unsupported image type: ." + ext);
        }

        // decode first so we fail fast on bad payload / oversized image
        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            throw new ImageStorageException("Image payload is not valid base64", e);
        }
        if (imageBytes.length > MAX_IMAGE_BYTES) {
            throw new ImageStorageException("Image exceeds the " + (MAX_IMAGE_BYTES / 1024 / 1024) + " MB limit");
        }

        try {
            // belt-and-suspenders: even after the sanitization above, resolve + verify
            // the target stays under baseDir. Last line of defense against traversal.
            Path baseDir = Paths.get(baseImagePath).toAbsolutePath().normalize();
            Path targetDir = baseDir.resolve(username).resolve(String.valueOf(tourId)).normalize();
            Path targetFile = targetDir.resolve(safeFilename).normalize();
            if (!targetFile.startsWith(baseDir)) {
                throw new ImageStorageException("Refusing to write outside of the image directory");
            }

            Files.createDirectories(targetDir);
            Files.write(targetFile, imageBytes);
            log.info("FINAL ABSOLUTE FILE PATH: {}", targetFile.toAbsolutePath());
            log.info("Saved image for user '{}' tour id={} -> {}", username, tourId, targetFile);

            return username + "/" + tourId + "/" + safeFilename;
        } catch (IOException e) {
            throw new ImageStorageException("Failed to write image to disk: " + e.getMessage(), e);
        }
    }

    private void deleteImage(String imagePath){
        try {
            Path baseDir = Paths.get(baseImagePath).toAbsolutePath().normalize();

            // imagePath is stored like: username/tourId/filename.jpg
            Path targetFile = baseDir.resolve(imagePath).normalize();

            // security check (same as save)
            if (!targetFile.startsWith(baseDir)) {
                throw new ImageStorageException("Refusing to delete outside of image directory");
            }

            boolean deleted = Files.deleteIfExists(targetFile);

            if (deleted) {
                log.info("Deleted image: {}", targetFile);
            } else {
                log.warn("Image not found for deletion: {}", targetFile);
            }

        } catch (IOException e) {
            throw new ImageStorageException("Failed to delete image: " + e.getMessage(), e);
        }
    }

    private void deleteAllImages() {
        try {
            Path baseDir = Paths.get(baseImagePath).toAbsolutePath().normalize();

            FileUtils.cleanDirectory(baseDir.toFile());

            log.info("Cleaned image directory: {}", baseDir);

        } catch (IOException e) {
            throw new ImageStorageException("Failed to clean image directory", e);
        }
    }
}
