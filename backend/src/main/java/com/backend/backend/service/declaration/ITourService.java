package com.backend.backend.service.declaration;

import com.backend.backend.model.dto.TourRequest;
import com.backend.backend.model.dto.TourResponse;

import java.util.List;

public interface ITourService {
    TourResponse createTour(TourRequest request, String username);
    TourResponse getTour(int id, String username);
    List<TourResponse> getAllTours(String username);
    TourResponse updateTour(int id, TourRequest request, String username);
    void deleteTour(int id, String username);
}
