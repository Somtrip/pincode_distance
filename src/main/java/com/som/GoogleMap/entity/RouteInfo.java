package com.som.GoogleMap.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "route_info",
       uniqueConstraints = {@UniqueConstraint(columnNames = {"fromPincode", "toPincode"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromPincode;
    private String toPincode;
    private Double distanceKm;
    private Double durationMinutes;

    @Lob
    private String routeJson;

    private LocalDateTime createdAt = LocalDateTime.now();
}

