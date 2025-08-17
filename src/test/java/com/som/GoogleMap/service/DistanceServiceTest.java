package com.som.GoogleMap.service;

import com.som.GoogleMap.client.GoogleMapsClient;
import com.som.GoogleMap.dto.GoogleRouteData;
import com.som.GoogleMap.dto.RouteResponse;
import com.som.GoogleMap.entity.RouteInfo;
import com.som.GoogleMap.repository.RouteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistanceServiceTest {

    @Mock private RouteRepository routeRepository;
    @Mock private GoogleMapsClient googleMapsClient;

    @InjectMocks private DistanceService distanceService;

    @Test
    void getDistance_returnsCached() {
        String from = "560001", to = "110001";

        RouteInfo entity = new RouteInfo();
        entity.setId(1L);
        entity.setFromPincode(from);
        entity.setToPincode(to);
        entity.setDistanceKm(15.0);
        entity.setDurationMinutes(25.0);
        entity.setRouteJson("{}");

        when(routeRepository.findByFromPincodeAndToPincode(from, to))
                .thenReturn(Optional.of(entity));

        RouteResponse res = distanceService.getDistance(from, to);

        assertEquals(from, res.getFromPincode());
        assertEquals(to, res.getToPincode());
        assertEquals(15.0, res.getDistanceKm());
        assertEquals(25.0, res.getDurationMinutes());
        verifyNoInteractions(googleMapsClient);
        verify(routeRepository, never()).save(any());
    }

    @Test
    void getDistance_fetchesAndSaves_whenNotCached() {
        String from = "560001", to = "110001";

        when(routeRepository.findByFromPincodeAndToPincode(from, to))
                .thenReturn(Optional.empty());

        GoogleRouteData api = new GoogleRouteData(12.0, 30.0, "{}");
        when(googleMapsClient.fetchRoute(from, to)).thenReturn(api);

        RouteInfo saved = new RouteInfo();
        saved.setId(99L);
        saved.setFromPincode(from);
        saved.setToPincode(to);
        saved.setDistanceKm(12.0);
        saved.setDurationMinutes(30.0);
        saved.setRouteJson("{}");
        when(routeRepository.save(any(RouteInfo.class))).thenReturn(saved);

        RouteResponse res = distanceService.getDistance(from, to);

        assertEquals(from, res.getFromPincode());
        assertEquals(to, res.getToPincode());
        assertEquals(12.0, res.getDistanceKm());
        assertEquals(30.0, res.getDurationMinutes());
        verify(googleMapsClient).fetchRoute(from, to);
        verify(routeRepository).save(any(RouteInfo.class));
    }
}
