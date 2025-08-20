package com.som.GoogleMap.service;

import com.som.GoogleMap.client.GoogleGeocodingClient;
import com.som.GoogleMap.client.GoogleMapsClient;
import com.som.GoogleMap.dto.GoogleRouteData;
import com.som.GoogleMap.dto.RouteResponse;
import com.som.GoogleMap.entity.PincodeInfo;
import com.som.GoogleMap.entity.RouteInfo;
import com.som.GoogleMap.repository.PincodeRepository;
import com.som.GoogleMap.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistanceServiceTest {

    @Mock 
    private RouteRepository routeRepository;
    
    @Mock
    private PincodeRepository pincodeRepository;
    
    @Mock 
    private GoogleMapsClient googleMapsClient;
    
    @Mock
    private GoogleGeocodingClient geocodingClient;
    
    @InjectMocks 
    private DistanceService distanceService;
    
    private final String fromPincode = "560001";
    private final String toPincode = "110001";
    
    @BeforeEach
    void setUp() {
        
        distanceService.setTtlHours(24);
    }
    
    @Test
    void getDistance_returnsCachedResult_whenAvailableAndFresh() {

        RouteInfo cachedRoute = createRouteInfo(fromPincode, toPincode, 15.0, 25.0, "{}");
        
        when(routeRepository.findByFromPincodeAndToPincode(fromPincode, toPincode))
                .thenReturn(Optional.of(cachedRoute));
        when(pincodeRepository.findByPincode(anyString()))
                .thenReturn(Optional.of(new PincodeInfo()));
        
        
        RouteResponse result = distanceService.getDistance(fromPincode, toPincode);
        

        assertEquals(fromPincode, result.getFromPincode());
        assertEquals(toPincode, result.getToPincode());
        assertEquals(15.0, result.getDistanceKm());
        assertEquals(25.0, result.getDurationMinutes());
        assertEquals("25 min", result.getDurationFormatted());
        assertEquals("{}", result.getRouteJson());
        
        verifyNoInteractions(googleMapsClient);
        verify(routeRepository, never()).save(any());
    }
    
    @Test
    void getDistance_usesReverseCache_whenAvailableAndFresh() {

        RouteInfo reverseRoute = createRouteInfo(toPincode, fromPincode, 15.0, 25.0, "{}");
        
        when(routeRepository.findByFromPincodeAndToPincode(fromPincode, toPincode))
                .thenReturn(Optional.empty());
        when(routeRepository.findByFromPincodeAndToPincode(toPincode, fromPincode))
                .thenReturn(Optional.of(reverseRoute));
        when(pincodeRepository.findByPincode(anyString()))
                .thenReturn(Optional.of(new PincodeInfo()));
        
        
        RouteResponse result = distanceService.getDistance(fromPincode, toPincode);
        
        
        assertEquals(fromPincode, result.getFromPincode());
        assertEquals(toPincode, result.getToPincode());
        assertEquals(15.0, result.getDistanceKm());
        assertEquals(25.0, result.getDurationMinutes());
        
        verifyNoInteractions(googleMapsClient);
        verify(routeRepository, never()).save(any());
    }
    
    @Test
    void getDistance_fetchesFromGoogle_whenNotCached() {
    
        GoogleRouteData apiData = new GoogleRouteData(12.0, 30.0, "{\"routes\":[]}");
        RouteInfo savedRoute = createRouteInfo(fromPincode, toPincode, 12.0, 30.0, "{\"routes\":[]}");
        
        when(routeRepository.findByFromPincodeAndToPincode(fromPincode, toPincode))
                .thenReturn(Optional.empty());
        when(routeRepository.findByFromPincodeAndToPincode(toPincode, fromPincode))
                .thenReturn(Optional.empty());
        when(googleMapsClient.fetchRoute(fromPincode, toPincode))
                .thenReturn(apiData);
        when(routeRepository.save(any(RouteInfo.class)))
                .thenReturn(savedRoute);
        when(pincodeRepository.findByPincode(anyString()))
                .thenReturn(Optional.of(new PincodeInfo()));
        
        
        RouteResponse result = distanceService.getDistance(fromPincode, toPincode);
        

        assertEquals(fromPincode, result.getFromPincode());
        assertEquals(toPincode, result.getToPincode());
        assertEquals(12.0, result.getDistanceKm());
        assertEquals(30.0, result.getDurationMinutes());
        assertEquals("30 min", result.getDurationFormatted());
        assertEquals("{\"routes\":[]}", result.getRouteJson());
        
        verify(googleMapsClient).fetchRoute(fromPincode, toPincode);
        verify(routeRepository).save(any(RouteInfo.class));
    }
    
    @Test
    void getDistance_updatesExistingCache_whenStale() {
        
        RouteInfo staleRoute = createRouteInfo(fromPincode, toPincode, 15.0, 25.0, "{}");
        staleRoute.setCreatedAt(LocalDateTime.now().minusHours(25));
        
        GoogleRouteData apiData = new GoogleRouteData(12.0, 30.0, "{\"routes\":[]}");
        RouteInfo updatedRoute = createRouteInfo(fromPincode, toPincode, 12.0, 30.0, "{\"routes\":[]}");
        
        when(routeRepository.findByFromPincodeAndToPincode(fromPincode, toPincode))
                .thenReturn(Optional.of(staleRoute));
        when(googleMapsClient.fetchRoute(fromPincode, toPincode))
                .thenReturn(apiData);
        when(routeRepository.save(any(RouteInfo.class)))
                .thenReturn(updatedRoute);
        when(pincodeRepository.findByPincode(anyString()))
                .thenReturn(Optional.of(new PincodeInfo()));
        
        
        RouteResponse result = distanceService.getDistance(fromPincode, toPincode);
        
        
        assertEquals(fromPincode, result.getFromPincode());
        assertEquals(toPincode, result.getToPincode());
        assertEquals(12.0, result.getDistanceKm());
        assertEquals(30.0, result.getDurationMinutes());
        
        verify(googleMapsClient).fetchRoute(fromPincode, toPincode);
        verify(routeRepository).save(any(RouteInfo.class));
    }
    
    @Test
    void getDistance_throwsException_whenInvalidPincode() {

        assertThrows(IllegalArgumentException.class, () -> {
            distanceService.getDistance("123", "456789");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            distanceService.getDistance("123456", "abc");
        });
    }
    
    private RouteInfo createRouteInfo(String from, String to, double distance, double duration, String routeJson) {
        RouteInfo route = new RouteInfo();
        route.setFromPincode(from);
        route.setToPincode(to);
        route.setDistanceKm(distance);
        route.setDurationMinutes(duration);
        route.setRouteJson(routeJson);
        route.setCreatedAt(LocalDateTime.now());
        return route;
    }
}