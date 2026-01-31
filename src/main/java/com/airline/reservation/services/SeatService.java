package com.airline.reservation.services;

import com.airline.reservation.dtos.SeatResponseDto;
import com.airline.reservation.exceptions.ResourceNotFoundException;
import com.airline.reservation.models.Flight;
import com.airline.reservation.models.Seat;
import com.airline.reservation.repositories.FlightRepository;
import com.airline.reservation.repositories.SeatRepository;
import com.airline.reservation.configs.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable(cacheNames = CacheConfig.CACHE_SEATS, key = "'flight:' + #flightId")
    public List<SeatResponseDto> getSeatsByFlightId(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", flightId));
        
        return seatRepository.findByFlight(flight).stream()
                .map(seat -> modelMapper.map(seat, SeatResponseDto.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CACHE_SEATS, key = "'available:' + #flightId")
    public List<SeatResponseDto> getAvailableSeatsByFlightId(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", flightId));
        
        return seatRepository.findByFlightAndStatus(flight, Seat.SeatStatus.AVAILABLE).stream()
                .map(seat -> modelMapper.map(seat, SeatResponseDto.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CACHE_SEATS, key = "#id")
    public SeatResponseDto getSeatById(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", id));
        
        return modelMapper.map(seat, SeatResponseDto.class);
    }
}

