package com.backend.backend.service.implementation;

import com.backend.backend.exception.RouteNotFoundException;
import com.backend.backend.model.dto.RouteResult;
import com.backend.backend.service.declaration.IOpenRouteService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenRouteServiceImpl implements IOpenRouteService {

    private static final String ORS_BASE_URL = "https://api.openrouteservice.org";

    @Value("${openrouteservice.apiKey}")
    private String apiKey;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public OpenRouteServiceImpl() {
        this.restClient = RestClient.builder().baseUrl(ORS_BASE_URL).build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public RouteResult getRoute(String from, String to, String transportType) {
        double[] fromCoords = geocode(from);
        double[] toCoords = geocode(to);
        try {
            return fetchRoute(fromCoords, toCoords, transportType);
        } catch (HttpClientErrorException e) {
            throw new RouteNotFoundException(from, to);
        }
    }

    private double[] geocode(String place) {
        try {
            String json = restClient.get()
                    .uri("/geocode/search?api_key={key}&text={text}&size=1", apiKey, place)
                    .retrieve()
                    .body(String.class);

            JsonNode features = objectMapper.readTree(json).path("features");
            if (features.isEmpty()) {
                throw new RuntimeException("Location not found: " + place);
            }
            JsonNode coords = features.get(0).path("geometry").path("coordinates");
            return new double[]{coords.get(0).asDouble(), coords.get(1).asDouble()};
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse geocoding response for: " + place, e);
        }
    }

    private RouteResult fetchRoute(double[] from, double[] to, String transportType) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "coordinates", List.of(
                            List.of(from[0], from[1]),
                            List.of(to[0], to[1])
                    )
            );

            String json = restClient.post()
                    .uri("/v2/directions/{profile}/geojson", transportType)
                    .header("Authorization", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(json);
            JsonNode summary = root.path("features").get(0).path("properties").path("summary");
            double distance = summary.path("distance").asDouble();
            int estimatedTime = (int) summary.path("duration").asDouble();

            return new RouteResult(distance, estimatedTime, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse route response from OpenRouteService", e);
        }
    }
}
