package com.airline.reservation.services;

import com.airline.reservation.dtos.FlightRequestDto;
import com.airline.reservation.dtos.FlightResponseDto;
import com.airline.reservation.exceptions.InvalidBookingException;
import com.airline.reservation.exceptions.ResourceAlreadyExistsException;
import com.airline.reservation.exceptions.ResourceNotFoundException;
import com.airline.reservation.models.Airport;
import com.airline.reservation.models.Flight;
import com.airline.reservation.models.Seat;
import com.airline.reservation.repositories.AirportRepository;
import com.airline.reservation.repositories.FlightRepository;
import com.airline.reservation.repositories.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FlightService Unit Tests")
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private SeatRepository seatRepository;

    // ModelMapper is no longer mocked, using a real instance
    private ModelMapper modelMapper;

    // @InjectMocks is removed as we are manually creating the service
    private FlightService flightService;

    private FlightRequestDto flightRequest;
    private Airport departureAirport;
    private Airport arrivalAirport;
    private Flight savedFlight;

    @BeforeEach
    void setUp() {
        // Use real ModelMapper instead of mocking (avoids Java 24 compatibility issues)
        modelMapper = new ModelMapper();

        // Since @InjectMocks won't work with a real instance, we'll create the service manually
        flightService = new FlightService(flightRepository, airportRepository, seatRepository, modelMapper);

        departureAirport = new Airport();
        departureAirport.setId(1L);
        departureAirport.setCode("JFK");
        departureAirport.setName("John F. Kennedy International Airport");
        departureAirport.setCity("New York");
        departureAirport.setCountry("USA");

        arrivalAirport = new Airport();
        arrivalAirport.setId(2L);
        arrivalAirport.setCode("LAX");
        arrivalAirport.setName("Los Angeles International Airport");
        arrivalAirport.setCity("Los Angeles");
        arrivalAirport.setCountry("USA");

        flightRequest = new FlightRequestDto();
        flightRequest.setFlightNumber("AA101");
        flightRequest.setAirlineName("American Airlines");
        flightRequest.setDepartureAirportId(1L);
        flightRequest.setArrivalAirportId(2L);
        flightRequest.setDepartureTime(LocalDateTime.now().plusDays(1));
        flightRequest.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(5));
        flightRequest.setTotalSeats(150);
        flightRequest.setBasePrice(500.0);

        savedFlight = new Flight();
        savedFlight.setId(1L);
        savedFlight.setFlightNumber("AA101");
        savedFlight.setAirlineName("American Airlines");
        savedFlight.setDepartureAirport(departureAirport);
        savedFlight.setArrivalAirport(arrivalAirport);
        savedFlight.setDepartureTime(flightRequest.getDepartureTime());
        savedFlight.setArrivalTime(flightRequest.getArrivalTime());
        savedFlight.setTotalSeats(150);
        savedFlight.setAvailableSeats(150);
        savedFlight.setBasePrice(500.0);
        savedFlight.setStatus(Flight.FlightStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Should successfully create a flight with seats")
    void testCreateFlight_Success() {
        // Given
        when(flightRepository.findByFlightNumber(anyString())).thenReturn(Optional.empty());
        when(airportRepository.findById(1L)).thenReturn(Optional.of(departureAirport));
        when(airportRepository.findById(2L)).thenReturn(Optional.of(arrivalAirport));
        when(flightRepository.save(any(Flight.class))).thenReturn(savedFlight);
        when(seatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        FlightResponseDto result = flightService.createFlight(flightRequest);

        // Then
        assertNotNull(result);
        verify(flightRepository).findByFlightNumber("AA101");
        verify(airportRepository).findById(1L);
        verify(airportRepository).findById(2L);
        verify(flightRepository).save(any(Flight.class));
        verify(seatRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when flight number already exists")
    void testCreateFlight_FlightNumberExists() {
        // Given
        when(flightRepository.findByFlightNumber(anyString())).thenReturn(Optional.of(savedFlight));

        // When & Then
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            flightService.createFlight(flightRequest);
        });

        verify(flightRepository).findByFlightNumber("AA101");
        verify(airportRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when departure airport not found")
    void testCreateFlight_DepartureAirportNotFound() {
        // Given
        when(flightRepository.findByFlightNumber(anyString())).thenReturn(Optional.empty());
        when(airportRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            flightService.createFlight(flightRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when arrival airport not found")
    void testCreateFlight_ArrivalAirportNotFound() {
        // Given
        when(flightRepository.findByFlightNumber(anyString())).thenReturn(Optional.empty());
        when(airportRepository.findById(1L)).thenReturn(Optional.of(departureAirport));
        when(airportRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            flightService.createFlight(flightRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when departure and arrival airports are the same")
    void testCreateFlight_SameAirports() {
        // Given
        flightRequest.setArrivalAirportId(1L); // Same as departure

        when(flightRepository.findByFlightNumber(anyString())).thenReturn(Optional.empty());
        when(airportRepository.findById(1L)).thenReturn(Optional.of(departureAirport));

        // When & Then
        assertThrows(InvalidBookingException.class, () -> {
            flightService.createFlight(flightRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when arrival time is before departure time")
    void testCreateFlight_InvalidArrivalTime() {
        // Given
        flightRequest.setArrivalTime(flightRequest.getDepartureTime().minusHours(1));

        when(flightRepository.findByFlightNumber(anyString())).thenReturn(Optional.empty());
        when(airportRepository.findById(1L)).thenReturn(Optional.of(departureAirport));
        when(airportRepository.findById(2L)).thenReturn(Optional.of(arrivalAirport));

        // When & Then
        assertThrows(InvalidBookingException.class, () -> {
            flightService.createFlight(flightRequest);
        });
    }

    @Test
    @DisplayName("Should create seats for flight based on total seats")
    void testCreateFlight_SeatGeneration() {
        // Given
        when(flightRepository.findByFlightNumber(anyString())).thenReturn(Optional.empty());
        when(airportRepository.findById(1L)).thenReturn(Optional.of(departureAirport));
        when(airportRepository.findById(2L)).thenReturn(Optional.of(arrivalAirport));
        when(flightRepository.save(any(Flight.class))).thenReturn(savedFlight);
        
        ArgumentCaptor<List<Seat>> seatsCaptor = ArgumentCaptor.forClass(List.class);
        when(seatRepository.saveAll(seatsCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        flightService.createFlight(flightRequest);

        // Then
        List<Seat> createdSeats = seatsCaptor.getValue();
        assertNotNull(createdSeats);
        assertFalse(createdSeats.isEmpty());
        
        // Verify seat generation logic (6 seats per row for 150 seats = ~25 rows)
        verify(seatRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should set available seats equal to total seats on creation")
    void testCreateFlight_AvailableSeatsInitialization() {
        // Given
        ArgumentCaptor<Flight> flightCaptor = ArgumentCaptor.forClass(Flight.class);
        when(flightRepository.findByFlightNumber(anyString())).thenReturn(Optional.empty());
        when(airportRepository.findById(1L)).thenReturn(Optional.of(departureAirport));
        when(airportRepository.findById(2L)).thenReturn(Optional.of(arrivalAirport));
        when(flightRepository.save(flightCaptor.capture())).thenReturn(savedFlight);
        when(seatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        flightService.createFlight(flightRequest);

        // Then
        Flight capturedFlight = flightCaptor.getValue();
        assertEquals(150, capturedFlight.getAvailableSeats());
        assertEquals(150, capturedFlight.getTotalSeats());
    }
}

