package com.som.GoogleMap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteResponse {
    private String fromPincode;
    private String toPincode;
    private double distanceKm;
    private double durationMinutes;
    private String durationFormatted;
    private String routeJson;
}

