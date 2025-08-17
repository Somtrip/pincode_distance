package com.som.GoogleMap.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.som.GoogleMap.dto.GoogleRouteData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GoogleMapsClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.api.key}")
    private String apiKey;

    @Value("${google.api.distance-matrix-url}")
    private String distanceMatrixUrl;

    public GoogleRouteData fetchRoute(String from, String to) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(distanceMatrixUrl)
                    .queryParam("origins", from)
                    .queryParam("destinations", to)
                    .queryParam("key", apiKey)
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            JsonNode element = root.path("rows").get(0).path("elements").get(0);

            // numeric values
            double distanceMeters = element.path("distance").path("value").asDouble();
            double durationSeconds = element.path("duration").path("value").asDouble();

            // convert
            double distanceKm = distanceMeters / 1000.0;
            double durationMinutes = durationSeconds / 60.0;

            return new GoogleRouteData(distanceKm, durationMinutes, response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching route from Google Maps API", e);
        }
    }
}
