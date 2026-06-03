package com.backend.backend.service.declaration;

import com.backend.backend.model.dto.RouteResult;

public interface IOpenRouteService {
    RouteResult getRoute(String from, String to, String transportType);
}
