package com.som.GoogleMap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleRouteData {
    private double distanceKm;
    private double durationMinutes;
    private String routeJson;
}

