package com.airline.reservation.services;

import com.airline.reservation.dtos.AirportResponseDto;
import com.airline.reservation.dtos.ExternalFlightDto;
import com.airline.reservation.dtos.FlightResponseDto;
import com.airline.reservation.dtos.FlightSearchRequestDto;
import com.airline.reservation.dtos.UnifiedFlightDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FlightSearchService Unit Tests")
class FlightSearchServiceTest {

    @Mock
    private FlightService flightService;

    @Mock
    private ExternalFlightApiService externalFlightApiService;

    private FlightSearchService service;
    private FlightSearchRequestDto searchDto;

    @BeforeEach
    void setUp() {
        service = new FlightSearchService(flightService, externalFlightApiService);
        searchDto = new FlightSearchRequestDto();
        searchDto.setDepartureAirportCode("JFK");
        searchDto.setArrivalAirportCode("LAX");
        searchDto.setDepartureDate(LocalDate.of(2025, 2, 15));
    }

    @Test
    @DisplayName("searchUnified includeExternal false returns only internal")
    void searchUnified_internalOnly() {
        FlightResponseDto r = new FlightResponseDto();
        r.setId(1L);
        r.setFlightNumber("AA101");
        r.setAirlineName("American Airlines");
        r.setDepartureAirport(new AirportResponseDto(1L, "JFK", "JFK", "New York", "USA"));
        r.setArrivalAirport(new AirportResponseDto(2L, "LAX", "LAX", "Los Angeles", "USA"));
        r.setDepartureTime(LocalDateTime.of(2025, 2, 15, 10, 0));
        r.setArrivalTime(LocalDateTime.of(2025, 2, 15, 15, 0));
        r.setBasePrice(299.99);
        r.setTotalSeats(120);
        r.setAvailableSeats(118);
        r.setStatus("SCHEDULED");

        when(flightService.searchFlights(any(FlightSearchRequestDto.class))).thenReturn(List.of(r));

        List<UnifiedFlightDto> result = service.searchUnified(searchDto, false);

        assertNotNull(result);
        assertEquals(1, result.size());
        UnifiedFlightDto u = result.get(0);
        assertEquals(UnifiedFlightDto.SOURCE_INTERNAL, u.getSource());
        assertTrue(u.isBookable());
        assertEquals(1L, u.getInternalFlightId());
        assertEquals("AA101", u.getFlightNumber());
        assertEquals("JFK", u.getDepartureAirportCode());
        assertEquals("LAX", u.getArrivalAirportCode());
        verify(flightService).searchFlights(searchDto);
        verify(externalFlightApiService, never()).fetchFlightsByRoute(anyString(), anyString());
    }

    @Test
    @DisplayName("searchUnified includeExternal true merges internal and external")
    void searchUnified_internalAndExternal() {
        FlightResponseDto r = new FlightResponseDto();
        r.setId(1L);
        r.setFlightNumber("AA101");
        r.setAirlineName("American Airlines");
        r.setDepartureAirport(new AirportResponseDto(1L, "JFK", "JFK", "NY", "USA"));
        r.setArrivalAirport(new AirportResponseDto(2L, "LAX", "LAX", "LA", "USA"));
        r.setDepartureTime(LocalDateTime.of(2025, 2, 15, 10, 0));
        r.setArrivalTime(LocalDateTime.of(2025, 2, 15, 15, 0));
        r.setBasePrice(299.99);
        r.setTotalSeats(120);
        r.setAvailableSeats(118);
        r.setStatus("SCHEDULED");

        ExternalFlightDto e = new ExternalFlightDto();
        e.setId("DL205");
        e.setFlightNumber("205");
        e.setAirline("Delta");
        e.setOrigin("JFK");
        e.setDestination("LAX");
        e.setPrice(349.99);

        when(flightService.searchFlights(any(FlightSearchRequestDto.class))).thenReturn(List.of(r));
        when(externalFlightApiService.fetchFlightsByRoute("JFK", "LAX")).thenReturn(List.of(e));

        List<UnifiedFlightDto> result = service.searchUnified(searchDto, true);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(UnifiedFlightDto.SOURCE_INTERNAL, result.get(0).getSource());
        assertTrue(result.get(0).isBookable());
        assertEquals(UnifiedFlightDto.SOURCE_EXTERNAL, result.get(1).getSource());
        assertFalse(result.get(1).isBookable());
        assertNull(result.get(1).getInternalFlightId());
        assertEquals("205", result.get(1).getFlightNumber());
        verify(externalFlightApiService).fetchFlightsByRoute("JFK", "LAX");
    }
}
