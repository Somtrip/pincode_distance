package com.som.GoogleMap.service;

import com.som.GoogleMap.client.GoogleGeocodingClient;
import com.som.GoogleMap.client.GoogleMapsClient;
import com.som.GoogleMap.dto.GoogleRouteData;
import com.som.GoogleMap.dto.RouteResponse;
import com.som.GoogleMap.entity.PincodeInfo;
import com.som.GoogleMap.entity.RouteInfo;
import com.som.GoogleMap.repository.PincodeRepository;
import com.som.GoogleMap.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class DistanceService {

    @Autowired private RouteRepository routeRepository;
    @Autowired private PincodeRepository pincodeRepository;

    @Autowired private GoogleMapsClient googleMapsClient;
    @Autowired private GoogleGeocodingClient geocodingClient;

    @Value("${app.cache.ttl-hours:24}")
    private long ttlHours;

    public RouteResponse getDistance(String from, String to) {
        validatePincode(from);
        validatePincode(to);

        ensurePincodeInfo(from);
        ensurePincodeInfo(to);

        RouteInfo cached = routeRepository.findByFromPincodeAndToPincode(from, to).orElse(null);
        if (isFresh(cached)) {
            return toResponse(cached);
        }

        
        RouteInfo reverse = routeRepository.findByFromPincodeAndToPincode(to, from).orElse(null);
        if (isFresh(reverse)) {
            return new RouteResponse(from, to,
                    reverse.getDistanceKm(),
                    reverse.getDurationMinutes(),
                    format(reverse.getDurationMinutes()),
                    reverse.getRouteJson());
        }

        
        GoogleRouteData data = googleMapsClient.fetchRoute(from, to);
        RouteInfo saved = (cached == null)
                ? routeRepository.save(new RouteInfo(null, from, to, data.getDistanceKm(), data.getDurationMinutes(),
                                                     data.getRouteJson(), LocalDateTime.now()))
                : updateRoute(cached, data);

        return toResponse(saved);
    }


    private boolean isFresh(RouteInfo ri) {
        return ri != null && hoursAgo(ri.getCreatedAt()) < ttlHours;
    }
    
    private long hoursAgo(LocalDateTime t) {
        return ChronoUnit.HOURS.between(t, LocalDateTime.now());
    }
    
    private RouteInfo updateRoute(RouteInfo existing, GoogleRouteData data) {
        existing.setDistanceKm(data.getDistanceKm());
        existing.setDurationMinutes(data.getDurationMinutes());
        existing.setRouteJson(data.getRouteJson());
        existing.setCreatedAt(LocalDateTime.now());
        return routeRepository.save(existing);
    }
    
    private RouteResponse toResponse(RouteInfo ri) {
        return new RouteResponse(ri.getFromPincode(), ri.getToPincode(),
                ri.getDistanceKm(), ri.getDurationMinutes(), 
                format(ri.getDurationMinutes()), ri.getRouteJson());
    }
    
    private String format(double minutes) {
        int h = (int)(minutes / 60);
        int m = (int)Math.round(minutes % 60);
        return h > 0 ? (h + " hr " + m + " min") : (m + " min");
    }
    
    private void validatePincode(String pin) {
        if (pin == null || !pin.matches("\\d{6}")) {
            throw new IllegalArgumentException("Invalid pincode: must be 6 digits");
        }
    }
    
    private void ensurePincodeInfo(String pincode) {
        if (pincodeRepository.findByPincode(pincode).isPresent()) return;
        var geo = geocodingClient.geocodePincode(pincode);
        if (geo == null) return;
        PincodeInfo pi = new PincodeInfo(null, pincode, geo.lat(), geo.lng(), geo.polygonJson());
        pincodeRepository.save(pi);
    }



    public void setTtlHours(long ttlHours) {
        this.ttlHours = ttlHours;
    }
}