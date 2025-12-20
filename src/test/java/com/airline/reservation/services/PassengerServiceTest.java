package com.airline.reservation.services;

import com.airline.reservation.dtos.PassengerRequestDto;
import com.airline.reservation.dtos.PassengerResponseDto;
import com.airline.reservation.exceptions.ResourceAlreadyExistsException;
import com.airline.reservation.exceptions.ResourceNotFoundException;
import com.airline.reservation.models.Passenger;
import com.airline.reservation.repositories.PassengerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PassengerService Unit Tests")
class PassengerServiceTest {

    @Mock
    private PassengerRepository passengerRepository;

    private ModelMapper modelMapper;
    private PassengerService passengerService;

    private PassengerRequestDto passengerRequest;
    private Passenger passenger;
    private PassengerResponseDto passengerResponse;

    @BeforeEach
    void setUp() {
        // Use real ModelMapper instead of mocking (avoids Java 24 compatibility issues)
        modelMapper = new ModelMapper();
        
        // Set the ModelMapper on the service using reflection or create service manually
        // Since @InjectMocks won't work with a real instance, we'll create the service manually
        passengerService = new PassengerService(passengerRepository, modelMapper);
        
        passengerRequest = new PassengerRequestDto();
        passengerRequest.setFirstName("John");
        passengerRequest.setLastName("Doe");
        passengerRequest.setEmail("john.doe@example.com");
        passengerRequest.setPhoneNumber("1234567890");
        passengerRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        passengerRequest.setPassportNumber("P123456");
        passengerRequest.setNationality("USA");

        passenger = new Passenger();
        passenger.setId(1L);
        passenger.setFirstName("John");
        passenger.setLastName("Doe");
        passenger.setEmail("john.doe@example.com");
        passenger.setPhoneNumber("1234567890");
        passenger.setDateOfBirth(LocalDate.of(1990, 1, 1));
        passenger.setPassportNumber("P123456");
        passenger.setNationality("USA");

        passengerResponse = new PassengerResponseDto();
        passengerResponse.setId(1L);
        passengerResponse.setFirstName("John");
        passengerResponse.setLastName("Doe");
        passengerResponse.setEmail("john.doe@example.com");
    }

    @Test
    @DisplayName("Should successfully create a passenger")
    void testCreatePassenger_Success() {
        // Given
        when(passengerRepository.existsByEmail(anyString())).thenReturn(false);
        when(passengerRepository.existsByPassportNumber(anyString())).thenReturn(false);
        when(passengerRepository.save(any(Passenger.class))).thenReturn(passenger);

        // When
        PassengerResponseDto result = passengerService.createPassenger(passengerRequest);

        // Then
        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(passengerRepository).existsByEmail("john.doe@example.com");
        verify(passengerRepository).existsByPassportNumber("P123456");
        verify(passengerRepository).save(any(Passenger.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testCreatePassenger_EmailExists() {
        // Given
        when(passengerRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            passengerService.createPassenger(passengerRequest);
        });

        verify(passengerRepository).existsByEmail("john.doe@example.com");
        verify(passengerRepository, never()).save(any(Passenger.class));
    }

    @Test
    @DisplayName("Should throw exception when passport number already exists")
    void testCreatePassenger_PassportExists() {
        // Given
        when(passengerRepository.existsByEmail(anyString())).thenReturn(false);
        when(passengerRepository.existsByPassportNumber(anyString())).thenReturn(true);

        // When & Then
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            passengerService.createPassenger(passengerRequest);
        });

        verify(passengerRepository).existsByPassportNumber("P123456");
        verify(passengerRepository, never()).save(any(Passenger.class));
    }

    @Test
    @DisplayName("Should successfully get passenger by ID")
    void testGetPassengerById_Success() {
        // Given
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));

        // When
        PassengerResponseDto result = passengerService.getPassengerById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("john.doe@example.com", result.getEmail());
        verify(passengerRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when passenger not found by ID")
    void testGetPassengerById_NotFound() {
        // Given
        when(passengerRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            passengerService.getPassengerById(1L);
        });
    }

    @Test
    @DisplayName("Should successfully get passenger by email")
    void testGetPassengerByEmail_Success() {
        // Given
        when(passengerRepository.findByEmail(anyString())).thenReturn(Optional.of(passenger));

        // When
        PassengerResponseDto result = passengerService.getPassengerByEmail("john.doe@example.com");

        // Then
        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
        verify(passengerRepository).findByEmail("john.doe@example.com");
    }

    @Test
    @DisplayName("Should throw exception when passenger not found by email")
    void testGetPassengerByEmail_NotFound() {
        // Given
        when(passengerRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            passengerService.getPassengerByEmail("nonexistent@example.com");
        });
    }

    @Test
    @DisplayName("Should successfully get all passengers")
    void testGetAllPassengers_Success() {
        // Given
        Passenger passenger2 = new Passenger();
        passenger2.setId(2L);
        passenger2.setFirstName("Jane");
        passenger2.setLastName("Doe");
        passenger2.setEmail("jane.doe@example.com");

        List<Passenger> passengers = Arrays.asList(passenger, passenger2);
        when(passengerRepository.findAll()).thenReturn(passengers);

        // When
        List<PassengerResponseDto> result = passengerService.getAllPassengers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("john.doe@example.com", result.get(0).getEmail());
        assertEquals("jane.doe@example.com", result.get(1).getEmail());
        verify(passengerRepository).findAll();
    }

    @Test
    @DisplayName("Should successfully update a passenger")
    void testUpdatePassenger_Success() {
        // Given
        PassengerRequestDto updateRequest = new PassengerRequestDto();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Doe");
        updateRequest.setEmail("john.doe@example.com"); // Same email
        updateRequest.setPhoneNumber("9876543210");
        updateRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        updateRequest.setPassportNumber("P123456");
        updateRequest.setNationality("USA");

        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(invocation -> {
            Passenger savedPassenger = invocation.getArgument(0);
            return savedPassenger;
        });

        // When
        PassengerResponseDto result = passengerService.updatePassenger(1L, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("9876543210", result.getPhoneNumber());
        verify(passengerRepository).findById(1L);
        verify(passengerRepository).save(any(Passenger.class));
    }

    @Test
    @DisplayName("Should throw exception when updating passenger with existing email")
    void testUpdatePassenger_EmailConflict() {
        // Given
        PassengerRequestDto updateRequest = new PassengerRequestDto();
        updateRequest.setEmail("existing@example.com"); // Different email that exists

        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(passengerRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            passengerService.updatePassenger(1L, updateRequest);
        });
    }

    @Test
    @DisplayName("Should successfully delete a passenger")
    void testDeletePassenger_Success() {
        // Given
        when(passengerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(passengerRepository).deleteById(1L);

        // When
        passengerService.deletePassenger(1L);

        // Then
        verify(passengerRepository).existsById(1L);
        verify(passengerRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent passenger")
    void testDeletePassenger_NotFound() {
        // Given
        when(passengerRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            passengerService.deletePassenger(1L);
        });

        verify(passengerRepository).existsById(1L);
        verify(passengerRepository, never()).deleteById(anyLong());
    }
}

