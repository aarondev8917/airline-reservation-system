package com.airline.reservation.controllers;

import com.airline.reservation.dtos.ExternalFlightDto;
import com.airline.reservation.dtos.FlightResponseDto;
import com.airline.reservation.dtos.ImportExternalFlightRequestDto;
import com.airline.reservation.dtos.UnifiedFlightDto;
import com.airline.reservation.services.ExternalFlightApiService;
import com.airline.reservation.services.FlightSearchService;
import com.airline.reservation.services.FlightService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = FlightController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.airline\\.reservation\\.security\\..*"
        )
)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("FlightController Unit Tests (Aviationstack integration)")
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FlightService flightService;

    @MockBean
    private ExternalFlightApiService externalFlightApiService;

    @MockBean
    private FlightSearchService flightSearchService;

    private ExternalFlightDto externalFlight;
    private UnifiedFlightDto unifiedFlight;

    @BeforeEach
    void setUp() {
        externalFlight = new ExternalFlightDto();
        externalFlight.setId("AA101");
        externalFlight.setFlightNumber("AA101");
        externalFlight.setAirline("American Airlines");
        externalFlight.setOrigin("JFK");
        externalFlight.setDestination("LAX");
        externalFlight.setPrice(299.99);

        unifiedFlight = new UnifiedFlightDto();
        unifiedFlight.setSource(UnifiedFlightDto.SOURCE_EXTERNAL);
        unifiedFlight.setBookable(false);
        unifiedFlight.setId("AA101");
        unifiedFlight.setFlightNumber("AA101");
        unifiedFlight.setAirlineName("American Airlines");
        unifiedFlight.setDepartureAirportCode("JFK");
        unifiedFlight.setArrivalAirportCode("LAX");
        unifiedFlight.setBasePrice(299.99);
    }

    @Test
    @DisplayName("GET /api/flights/external returns external flights")
    void getExternalFlights_success() throws Exception {
        when(externalFlightApiService.fetchAllExternalFlights()).thenReturn(List.of(externalFlight));

        mockMvc.perform(get("/api/flights/external"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("External flights retrieved successfully"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value("AA101"))
                .andExpect(jsonPath("$.data[0].flightNumber").value("AA101"));

        verify(externalFlightApiService).fetchAllExternalFlights();
    }

    @Test
    @DisplayName("GET /api/flights/external/number/{fn} returns filtered external flights")
    void getExternalFlightsByNumber_success() throws Exception {
        when(externalFlightApiService.fetchFlightsByFlightNumber("AA101")).thenReturn(List.of(externalFlight));

        mockMvc.perform(get("/api/flights/external/number/AA101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(externalFlightApiService).fetchFlightsByFlightNumber("AA101");
    }

    @Test
    @DisplayName("GET /api/flights/external/{id} returns single external flight")
    void getExternalFlightById_success() throws Exception {
        when(externalFlightApiService.fetchExternalFlightById("AA101")).thenReturn(externalFlight);

        mockMvc.perform(get("/api/flights/external/AA101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("AA101"));

        verify(externalFlightApiService).fetchExternalFlightById("AA101");
    }

    @Test
    @DisplayName("POST /api/flights/search-unified returns unified results")
    void searchUnified_success() throws Exception {
        when(flightSearchService.searchUnified(any(), eq(true))).thenReturn(List.of(unifiedFlight));

        mockMvc.perform(post("/api/flights/search-unified?includeExternal=true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"departureAirportCode\":\"JFK\",\"arrivalAirportCode\":\"LAX\",\"departureDate\":\"2025-02-15\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].source").value("external"))
                .andExpect(jsonPath("$.data[0].bookable").value(false));

        verify(flightSearchService).searchUnified(any(), eq(true));
    }

    @Test
    @DisplayName("POST /api/flights/import-from-external with externalFlightId returns 201")
    void importFromExternal_byId_success() throws Exception {
        FlightResponseDto created = new FlightResponseDto();
        created.setId(100L);
        created.setFlightNumber("AA101");
        when(externalFlightApiService.fetchExternalFlightById("AA101")).thenReturn(externalFlight);
        when(flightService.importExternalFlight(any(ExternalFlightDto.class))).thenReturn(created);

        ImportExternalFlightRequestDto req = new ImportExternalFlightRequestDto();
        req.setExternalFlightId("AA101");

        mockMvc.perform(post("/api/flights/import-from-external")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100));

        verify(externalFlightApiService).fetchExternalFlightById("AA101");
        verify(flightService).importExternalFlight(any(ExternalFlightDto.class));
    }

    @Test
    @DisplayName("POST /api/flights/import-from-external with missing body returns 400")
    void importFromExternal_missingBody_badRequest() throws Exception {
        mockMvc.perform(post("/api/flights/import-from-external")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
