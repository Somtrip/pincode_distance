package com.som.GoogleMap.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GoogleGeocodingClient {
    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${google.api.key}") private String apiKey;
    @Value("${google.api.geocode-url}") private String geocodeUrl;

    public GeocodeResult geocodePincode(String pincode) {
        String url = UriComponentsBuilder.fromHttpUrl(geocodeUrl)
                .queryParam("address", pincode)
                .queryParam("region", "in")
                .queryParam("key", apiKey)
                .toUriString();

        ResponseEntity<String> resp = rest.getForEntity(url, String.class);
        try {
            JsonNode root = mapper.readTree(resp.getBody());
            JsonNode result = root.path("results").get(0);
            if (result == null) return null;

            JsonNode loc = result.path("geometry").path("location");
            Double lat = loc.path("lat").isNumber() ? loc.path("lat").asDouble() : null;
            Double lng = loc.path("lng").isNumber() ? loc.path("lng").asDouble() : null;


            String polygonJson = result.path("geometry").path("viewport").toString();

            return new GeocodeResult(lat, lng, polygonJson, resp.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Geocoding API", e);
        }
    }

    public record GeocodeResult(Double lat, Double lng, String polygonJson, String rawJson) {}
}
