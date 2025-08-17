package com.som.GoogleMap.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.som.GoogleMap.dto.RouteResponse;
import com.som.GoogleMap.service.DistanceService;

@RestController
@RequestMapping("/api/distance")
public class DistanceController {

    @Autowired
    private DistanceService distanceService;

    @GetMapping
    public ResponseEntity<RouteResponse> getDistance(
            @RequestParam String from,
            @RequestParam String to) {
        return ResponseEntity.ok(distanceService.getDistance(from, to));
    }
}

