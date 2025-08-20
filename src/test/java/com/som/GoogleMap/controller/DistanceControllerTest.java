package com.som.GoogleMap.controller;

import com.som.GoogleMap.dto.RouteResponse;
import com.som.GoogleMap.service.DistanceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DistanceController.class)
class DistanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DistanceService distanceService;

    @Test
    void testGetDistanceEndpoint_returnsSingleRoute() throws Exception {
        RouteResponse mockResponse = new RouteResponse(
                "560001", "110001", 10.0, 15.0, "15 min", "{}");

        Mockito.when(distanceService.getDistance("560001", "110001"))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/distance")
                        .param("from", "560001")
                        .param("to", "110001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromPincode").value("560001"))
                .andExpect(jsonPath("$.toPincode").value("110001"))
                .andExpect(jsonPath("$.distanceKm").value(10.0))
                .andExpect(jsonPath("$.durationMinutes").value(15.0))
                .andExpect(jsonPath("$.durationFormatted").value("15 min"))
                .andExpect(jsonPath("$.routeJson").value("{}"));
    }
}