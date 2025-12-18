package com.airline.reservation.services;

import com.airline.reservation.dtos.PassengerRequestDto;
import com.airline.reservation.dtos.PassengerResponseDto;
import com.airline.reservation.exceptions.ResourceAlreadyExistsException;
import com.airline.reservation.exceptions.ResourceNotFoundException;
import com.airline.reservation.models.Passenger;
import com.airline.reservation.repositories.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Passenger operations
 */
@Service
@RequiredArgsConstructor
public class PassengerService {
    
    private final PassengerRepository passengerRepository;
    private final ModelMapper modelMapper;
    
    @Transactional
    public PassengerResponseDto createPassenger(PassengerRequestDto requestDto) {
        // Check if email already exists
        if (passengerRepository.existsByEmail(requestDto.getEmail())) {
            throw new ResourceAlreadyExistsException("Passenger", "email", requestDto.getEmail());
        }
        
        // Check if passport number already exists
        if (passengerRepository.existsByPassportNumber(requestDto.getPassportNumber())) {
            throw new ResourceAlreadyExistsException("Passenger", "passportNumber", requestDto.getPassportNumber());
        }
        
        Passenger passenger = modelMapper.map(requestDto, Passenger.class);
        Passenger savedPassenger = passengerRepository.save(passenger);
        
        return modelMapper.map(savedPassenger, PassengerResponseDto.class);
    }
    
    @Transactional(readOnly = true)
    public PassengerResponseDto getPassengerById(Long id) {
        Passenger passenger = passengerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger", id));
        
        return modelMapper.map(passenger, PassengerResponseDto.class);
    }
    
    @Transactional(readOnly = true)
    public PassengerResponseDto getPassengerByEmail(String email) {
        Passenger passenger = passengerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger", "email", email));
        
        return modelMapper.map(passenger, PassengerResponseDto.class);
    }
    
    @Transactional(readOnly = true)
    public List<PassengerResponseDto> getAllPassengers() {
        return passengerRepository.findAll().stream()
                .map(passenger -> modelMapper.map(passenger, PassengerResponseDto.class))
                .collect(Collectors.toList());
    }
    
    @Transactional
    public PassengerResponseDto updatePassenger(Long id, PassengerRequestDto requestDto) {
        Passenger passenger = passengerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger", id));
        
        // Check if email is being changed and already exists
        if (!passenger.getEmail().equals(requestDto.getEmail()) && 
            passengerRepository.existsByEmail(requestDto.getEmail())) {
            throw new ResourceAlreadyExistsException("Passenger", "email", requestDto.getEmail());
        }
        
        modelMapper.map(requestDto, passenger);
        Passenger updatedPassenger = passengerRepository.save(passenger);
        
        return modelMapper.map(updatedPassenger, PassengerResponseDto.class);
    }
    
    @Transactional
    public void deletePassenger(Long id) {
        if (!passengerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Passenger", id);
        }
        passengerRepository.deleteById(id);
    }
}

