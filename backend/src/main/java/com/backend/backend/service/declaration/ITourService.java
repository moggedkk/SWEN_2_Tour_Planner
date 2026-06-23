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

    // Multi-field search. All filters AND-combined:
    //  - start / end / transport = exact-ish substring match on the tour's own fields
    //  - query = full-text search across tour fields, all the tour's logs, and the
    //    computed attributes (popularity, child-friendliness)
    // Any of these can be empty/null — empty means "don't filter on this one".
    // If all four are empty you get every tour for the user.
    List<TourResponse> searchTours(String start, String end, String transport, String query, String username);
}
