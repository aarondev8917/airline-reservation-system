package com.airline.reservation.services;

import com.airline.reservation.dtos.AviationstackResponseDto;
import com.airline.reservation.dtos.ExternalFlightDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalFlightApiService Unit Tests")
class ExternalFlightApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private ExternalFlightApiService service;

    @BeforeEach
    void setUp() {
        service = new ExternalFlightApiService(restTemplate);
        ReflectionTestUtils.setField(service, "useMock", true);
        ReflectionTestUtils.setField(service, "apiKey", "");
    }

    @Test
    @DisplayName("fetchAllExternalFlights returns mock data when useMock true")
    void fetchAllExternalFlights_useMock_returnsMockData() {
        List<ExternalFlightDto> result = service.fetchAllExternalFlights();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.size() >= 8);
        verify(restTemplate, never()).getForEntity(anyString(), any(Class.class));
    }

    @Test
    @DisplayName("fetchFlightsByFlightNumber filters mock by flight number")
    void fetchFlightsByFlightNumber_useMock_filtersMockData() {
        List<ExternalFlightDto> result = service.fetchFlightsByFlightNumber("AA101");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(f -> "AA101".equalsIgnoreCase(f.getFlightNumber())));
        verify(restTemplate, never()).getForEntity(anyString(), any(Class.class));
    }

    @Test
    @DisplayName("fetchFlightsByRoute filters mock by dep/arr")
    void fetchFlightsByRoute_useMock_filtersByRoute() {
        List<ExternalFlightDto> result = service.fetchFlightsByRoute("JFK", "LAX");

        assertNotNull(result);
        assertTrue(result.stream().allMatch(f ->
                "JFK".equalsIgnoreCase(f.getOrigin()) && "LAX".equalsIgnoreCase(f.getDestination())));
        verify(restTemplate, never()).getForEntity(anyString(), any(Class.class));
    }

    @Test
    @DisplayName("fetchExternalFlightById returns mock when useMock true")
    void fetchExternalFlightById_useMock_returnsMock() {
        ExternalFlightDto result = service.fetchExternalFlightById("1");

        assertNotNull(result);
        assertEquals("1", result.getId());
        verify(restTemplate, never()).getForEntity(anyString(), any(Class.class));
    }

    @Test
    @DisplayName("fetchAllExternalFlights falls back to mock when API throws")
    void fetchAllExternalFlights_apiFails_fallbackToMock() {
        ReflectionTestUtils.setField(service, "useMock", false);
        ReflectionTestUtils.setField(service, "apiKey", "fake-key");
        when(restTemplate.getForEntity(anyString(), eq(AviationstackResponseDto.class)))
                .thenThrow(new RestClientException("Connection refused"));

        List<ExternalFlightDto> result = service.fetchAllExternalFlights();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
