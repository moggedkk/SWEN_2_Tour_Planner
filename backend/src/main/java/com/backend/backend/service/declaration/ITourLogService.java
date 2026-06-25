package com.backend.backend.service.declaration;

import com.backend.backend.model.dto.TourLogRequest;
import com.backend.backend.model.dto.TourLogResponse;
import com.backend.backend.model.entity.Tour;

import java.util.List;

// The interface defines WHAT the service can do.
// The Impl class defines HOW it does it.
// Splitting them like this is the Strategy/DI pattern — lets us swap implementations later
// (e.g. for tests) and is what the spec wants when it talks about layered architecture.
public interface ITourLogService {

    TourLogResponse createLog(int tourId, TourLogRequest request, String username);

    List<TourLogResponse> getLogsForTour(int tourId, String username);

    TourLogResponse updateLog(int tourId, int logId, TourLogRequest request, String username);

    void deleteLog(int tourId, int logId, String username);

    // used internally when a whole tour is deleted — wipes all its logs first
    void deleteAllLogsForTour(Tour tour);

    String createImage(String imageEncoded, String imageName, String userName, String tourName);
}
