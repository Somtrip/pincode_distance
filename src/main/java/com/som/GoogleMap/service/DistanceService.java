package com.som.GoogleMap.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.som.GoogleMap.client.GoogleMapsClient;
import com.som.GoogleMap.dto.GoogleRouteData;
import com.som.GoogleMap.dto.RouteResponse;
import com.som.GoogleMap.entity.RouteInfo;
import com.som.GoogleMap.repository.RouteRepository;

@Service
public class DistanceService {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private GoogleMapsClient googleMapsClient;

    public RouteResponse getDistance(String from, String to) {
        // 1. Check cache
        return routeRepository.findByFromPincodeAndToPincode(from, to)
                .map(route -> new RouteResponse(route.getFromPincode(),
                                                route.getToPincode(),
                                                route.getDistanceKm(),
                                                route.getDurationMinutes()))
                .orElseGet(() -> {
                    // 2. Call Google API
                    GoogleRouteData data = googleMapsClient.fetchRoute(from, to);

                    // 3. Save in DB
                    RouteInfo saved = routeRepository.save(
                            new RouteInfo(null, from, to,
                                          data.getDistanceKm(),
                                          data.getDurationMinutes(),
                                          data.getRouteJson(),
                                          LocalDateTime.now())
                    );

                    // 4. Return response
                    return new RouteResponse(saved.getFromPincode(),
                                             saved.getToPincode(),
                                             saved.getDistanceKm(),
                                             saved.getDurationMinutes());
                });
    }
}

