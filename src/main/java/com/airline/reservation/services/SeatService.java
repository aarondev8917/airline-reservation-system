package com.airline.reservation.services;

import com.airline.reservation.dtos.SeatResponseDto;
import com.airline.reservation.exceptions.ResourceNotFoundException;
import com.airline.reservation.models.Flight;
import com.airline.reservation.models.Seat;
import com.airline.reservation.repositories.FlightRepository;
import com.airline.reservation.repositories.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Seat operations
 */
@Service
@RequiredArgsConstructor
public class SeatService {
    
    private final SeatRepository seatRepository;
    private final FlightRepository flightRepository;
    private final ModelMapper modelMapper;
    
    @Transactional(readOnly = true)
    public List<SeatResponseDto> getSeatsByFlightId(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", flightId));
        
        return seatRepository.findByFlight(flight).stream()
                .map(seat -> modelMapper.map(seat, SeatResponseDto.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<SeatResponseDto> getAvailableSeatsByFlightId(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", flightId));
        
        return seatRepository.findByFlightAndStatus(flight, Seat.SeatStatus.AVAILABLE).stream()
                .map(seat -> modelMapper.map(seat, SeatResponseDto.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public SeatResponseDto getSeatById(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", id));
        
        return modelMapper.map(seat, SeatResponseDto.class);
    }
}

