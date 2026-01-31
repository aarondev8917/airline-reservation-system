package com.airline.reservation.dtos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AviationstackMapper Unit Tests")
class AviationstackMapperTest {

    @Test
    @DisplayName("Maps full Aviationstack flight to ExternalFlightDto")
    void toExternalFlightDto_full() {
        AviationstackFlightDto a = new AviationstackFlightDto();
        AviationstackFlightInfoDto flight = new AviationstackFlightInfoDto();
        flight.setNumber("101");
        flight.setIata("AA101");
        flight.setIcao("AAL101");
        a.setFlight(flight);

        AviationstackAirlineDto airline = new AviationstackAirlineDto();
        airline.setName("American Airlines");
        airline.setIata("AA");
        a.setAirline(airline);

        AviationstackAirportDto depAirport = new AviationstackAirportDto();
        depAirport.setIata("JFK");
        depAirport.setAirportName("JFK International");
        AviationstackAirportInfoDto dep = new AviationstackAirportInfoDto();
        dep.setIata("JFK");
        dep.setAirport(depAirport);
        dep.setScheduled("2025-02-15T10:30:00");
        a.setDeparture(dep);

        AviationstackAirportDto arrAirport = new AviationstackAirportDto();
        arrAirport.setIata("LAX");
        AviationstackAirportInfoDto arr = new AviationstackAirportInfoDto();
        arr.setIata("LAX");
        arr.setAirport(arrAirport);
        arr.setScheduled("2025-02-15T15:45:00");
        a.setArrival(arr);

        ExternalFlightDto dto = AviationstackMapper.toExternalFlightDto(a);

        assertEquals("AA101", dto.getId());
        assertEquals("101", dto.getFlightNumber());
        assertEquals("American Airlines", dto.getAirline());
        assertEquals("JFK", dto.getOrigin());
        assertEquals("LAX", dto.getDestination());
        assertEquals(299.99, dto.getPrice());
        assertEquals(LocalDateTime.of(2025, 2, 15, 10, 30, 0), dto.getDepartureTime());
        assertEquals(LocalDateTime.of(2025, 2, 15, 15, 45, 0), dto.getArrivalTime());
    }

    @Test
    @DisplayName("Uses flight number when IATA missing")
    void toExternalFlightDto_numberFallback() {
        AviationstackFlightDto a = new AviationstackFlightDto();
        AviationstackFlightInfoDto flight = new AviationstackFlightInfoDto();
        flight.setNumber("101");
        flight.setIata(null);
        a.setFlight(flight);
        a.setAirline(null);
        a.setDeparture(null);
        a.setArrival(null);

        ExternalFlightDto dto = AviationstackMapper.toExternalFlightDto(a);

        assertEquals("101", dto.getId());
        assertEquals("101", dto.getFlightNumber());
        assertNull(dto.getAirline());
        assertNull(dto.getOrigin());
        assertNull(dto.getDestination());
        assertEquals(399.99, dto.getPrice());
        assertNull(dto.getDepartureTime());
        assertNull(dto.getArrivalTime());
    }

    @Test
    @DisplayName("Handles null scheduled times")
    void toExternalFlightDto_nullScheduled() {
        AviationstackFlightDto a = new AviationstackFlightDto();
        AviationstackFlightInfoDto flight = new AviationstackFlightInfoDto();
        flight.setIata("BA501");
        flight.setNumber("501");
        a.setFlight(flight);
        AviationstackAirlineDto airline = new AviationstackAirlineDto();
        airline.setName("British Airways");
        a.setAirline(airline);
        AviationstackAirportInfoDto dep = new AviationstackAirportInfoDto();
        dep.setIata("LHR");
        dep.setAirport(new AviationstackAirportDto());
        dep.setScheduled(null);
        a.setDeparture(dep);
        AviationstackAirportInfoDto arr = new AviationstackAirportInfoDto();
        arr.setIata("JFK");
        arr.setAirport(new AviationstackAirportDto());
        arr.setScheduled(null);
        a.setArrival(arr);

        ExternalFlightDto dto = AviationstackMapper.toExternalFlightDto(a);

        assertEquals("BA501", dto.getId());
        assertEquals("501", dto.getFlightNumber());
        assertEquals("LHR", dto.getOrigin());
        assertEquals("JFK", dto.getDestination());
        // Mapper treats any two 3-letter codes as domestic
        assertEquals(299.99, dto.getPrice());
        assertNull(dto.getDepartureTime());
        assertNull(dto.getArrivalTime());
    }

    @Test
    @DisplayName("Handles ISO datetime with offset")
    void toExternalFlightDto_isoWithOffset() {
        AviationstackFlightDto a = new AviationstackFlightDto();
        AviationstackFlightInfoDto flight = new AviationstackFlightInfoDto();
        flight.setIata("EK701");
        flight.setNumber("701");
        a.setFlight(flight);
        a.setAirline(null);
        AviationstackAirportInfoDto dep = new AviationstackAirportInfoDto();
        dep.setIata("DXB");
        dep.setAirport(new AviationstackAirportDto());
        dep.setScheduled("2025-03-01T14:00:00+04:00");
        a.setDeparture(dep);
        a.setArrival(null);

        ExternalFlightDto dto = AviationstackMapper.toExternalFlightDto(a);

        assertEquals(LocalDateTime.of(2025, 3, 1, 14, 0, 0), dto.getDepartureTime());
    }
}
