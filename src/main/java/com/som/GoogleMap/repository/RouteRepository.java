package com.som.GoogleMap.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.som.GoogleMap.entity.RouteInfo;

public interface RouteRepository extends JpaRepository<RouteInfo, Long> {
    Optional<RouteInfo> findByFromPincodeAndToPincode(String from, String to);
}


