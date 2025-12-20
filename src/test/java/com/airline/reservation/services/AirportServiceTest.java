package com.airline.reservation.services;

import com.airline.reservation.dtos.AirportRequestDto;
import com.airline.reservation.dtos.AirportResponseDto;
import com.airline.reservation.exceptions.ResourceAlreadyExistsException;
import com.airline.reservation.exceptions.ResourceNotFoundException;
import com.airline.reservation.models.Airport;
import com.airline.reservation.repositories.AirportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AirportService Unit Tests")
class AirportServiceTest {

    @Mock
    private AirportRepository airportRepository;

    // ModelMapper is no longer mocked, using a real instance
    private ModelMapper modelMapper;

    // @InjectMocks is removed as we are manually creating the service
    private AirportService airportService;

    private AirportRequestDto airportRequest;
    private Airport airport;
    private AirportResponseDto airportResponse;

    @BeforeEach
    void setUp() {
        // Use real ModelMapper instead of mocking (avoids Java 24 compatibility issues)
        modelMapper = new ModelMapper();

        // Since @InjectMocks won't work with a real instance, we'll create the service manually
        airportService = new AirportService(airportRepository, modelMapper);
        airportRequest = new AirportRequestDto();
        airportRequest.setCode("JFK");
        airportRequest.setName("John F. Kennedy International Airport");
        airportRequest.setCity("New York");
        airportRequest.setCountry("USA");

        airport = new Airport();
        airport.setId(1L);
        airport.setCode("JFK");
        airport.setName("John F. Kennedy International Airport");
        airport.setCity("New York");
        airport.setCountry("USA");

        airportResponse = new AirportResponseDto();
        airportResponse.setId(1L);
        airportResponse.setCode("JFK");
        airportResponse.setName("John F. Kennedy International Airport");
        airportResponse.setCity("New York");
        airportResponse.setCountry("USA");
    }

    @Test
    @DisplayName("Should successfully create an airport")
    void testCreateAirport_Success() {
        // Given
        when(airportRepository.existsByCode(anyString())).thenReturn(false);
        when(airportRepository.save(any(Airport.class))).thenAnswer(invocation -> {
            Airport a = invocation.getArgument(0);
            a.setId(1L); // Set ID to match expected response
            return a;
        });

        // When
        AirportResponseDto result = airportService.createAirport(airportRequest);

        // Then
        assertNotNull(result);
        assertEquals("JFK", result.getCode());
        verify(airportRepository).existsByCode("JFK");
        verify(airportRepository).save(any(Airport.class));
    }

    @Test
    @DisplayName("Should convert airport code to uppercase")
    void testCreateAirport_CodeUpperCase() {
        // Given
        airportRequest.setCode("jfk"); // lowercase
        when(airportRepository.existsByCode("JFK")).thenReturn(false);
        when(airportRepository.save(any(Airport.class))).thenAnswer(invocation -> {
            Airport a = invocation.getArgument(0);
            assertEquals("JFK", a.getCode()); // Should be uppercase
            a.setId(1L);
            return a;
        });

        // When
        airportService.createAirport(airportRequest);

        // Then
        verify(airportRepository).existsByCode("JFK");
    }

    @Test
    @DisplayName("Should throw exception when airport code already exists")
    void testCreateAirport_CodeExists() {
        // Given
        when(airportRepository.existsByCode(anyString())).thenReturn(true);

        // When & Then
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            airportService.createAirport(airportRequest);
        });

        verify(airportRepository).existsByCode("JFK");
        verify(airportRepository, never()).save(any(Airport.class));
    }

    @Test
    @DisplayName("Should successfully get airport by ID")
    void testGetAirportById_Success() {
        // Given
        when(airportRepository.findById(1L)).thenReturn(Optional.of(airport));

        // When
        AirportResponseDto result = airportService.getAirportById(1L);

        // Then
        assertNotNull(result);
        assertEquals("JFK", result.getCode());
        verify(airportRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when airport not found by ID")
    void testGetAirportById_NotFound() {
        // Given
        when(airportRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            airportService.getAirportById(1L);
        });
    }

    @Test
    @DisplayName("Should successfully get airport by code")
    void testGetAirportByCode_Success() {
        // Given
        when(airportRepository.findByCode("JFK")).thenReturn(Optional.of(airport));

        // When
        AirportResponseDto result = airportService.getAirportByCode("JFK");

        // Then
        assertNotNull(result);
        verify(airportRepository).findByCode("JFK");
    }

    @Test
    @DisplayName("Should convert code to uppercase when getting by code")
    void testGetAirportByCode_CodeUpperCase() {
        // Given
        when(airportRepository.findByCode("JFK")).thenReturn(Optional.of(airport));

        // When
        airportService.getAirportByCode("jfk"); // lowercase input

        // Then
        verify(airportRepository).findByCode("JFK"); // Should be uppercase
    }

    @Test
    @DisplayName("Should successfully get all airports")
    void testGetAllAirports_Success() {
        // Given
        Airport airport2 = new Airport();
        airport2.setId(2L);
        airport2.setCode("LAX");

        AirportResponseDto response2 = new AirportResponseDto();
        response2.setId(2L);
        response2.setCode("LAX");

        List<Airport> airports = Arrays.asList(airport, airport2);
        when(airportRepository.findAll()).thenReturn(airports);

        // When
        List<AirportResponseDto> result = airportService.getAllAirports();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(airportRepository).findAll();
    }

    @Test
    @DisplayName("Should successfully get airports by city")
    void testGetAirportsByCity_Success() {
        // Given
        List<Airport> airports = Arrays.asList(airport);
        when(airportRepository.findByCity("New York")).thenReturn(airports);

        // When
        List<AirportResponseDto> result = airportService.getAirportsByCity("New York");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(airportRepository).findByCity("New York");
    }

    @Test
    @DisplayName("Should successfully update an airport")
    void testUpdateAirport_Success() {
        // Given
        AirportRequestDto updateRequest = new AirportRequestDto();
        updateRequest.setCode("JFK");
        updateRequest.setName("Updated Airport Name");
        updateRequest.setCity("New York");
        updateRequest.setCountry("USA");

        when(airportRepository.findById(1L)).thenReturn(Optional.of(airport));
        when(airportRepository.save(any(Airport.class))).thenAnswer(invocation -> {
            Airport a = invocation.getArgument(0);
            return a; // Return the updated airport
        });

        // When
        AirportResponseDto result = airportService.updateAirport(1L, updateRequest);

        // Then
        assertNotNull(result);
        verify(airportRepository).findById(1L);
        verify(airportRepository).save(any(Airport.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent airport")
    void testUpdateAirport_NotFound() {
        // Given
        when(airportRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            airportService.updateAirport(1L, airportRequest);
        });
    }

    @Test
    @DisplayName("Should successfully delete an airport")
    void testDeleteAirport_Success() {
        // Given
        when(airportRepository.existsById(1L)).thenReturn(true);
        doNothing().when(airportRepository).deleteById(1L);

        // When
        airportService.deleteAirport(1L);

        // Then
        verify(airportRepository).existsById(1L);
        verify(airportRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent airport")
    void testDeleteAirport_NotFound() {
        // Given
        when(airportRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            airportService.deleteAirport(1L);
        });

        verify(airportRepository, never()).deleteById(anyLong());
    }
}

