package com.airline.reservation.services;

import com.airline.reservation.dtos.AirportRequestDto;
import com.airline.reservation.dtos.AirportResponseDto;
import com.airline.reservation.exceptions.ResourceAlreadyExistsException;
import com.airline.reservation.exceptions.ResourceNotFoundException;
import com.airline.reservation.models.Airport;
import com.airline.reservation.repositories.AirportRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Airport operations
 */
@Service
@RequiredArgsConstructor
public class AirportService {
    
    private final AirportRepository airportRepository;
    private final ModelMapper modelMapper;
    
    @Transactional
    public AirportResponseDto createAirport(AirportRequestDto requestDto) {
        // Check if airport code already exists
        if (airportRepository.existsByCode(requestDto.getCode().toUpperCase())) {
            throw new ResourceAlreadyExistsException("Airport", "code", requestDto.getCode());
        }
        
        Airport airport = modelMapper.map(requestDto, Airport.class);
        airport.setCode(airport.getCode().toUpperCase()); // Ensure code is uppercase
        Airport savedAirport = airportRepository.save(airport);
        
        return modelMapper.map(savedAirport, AirportResponseDto.class);
    }
    
    @Transactional(readOnly = true)
    public AirportResponseDto getAirportById(Long id) {
        Airport airport = airportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Airport", id));
        
        return modelMapper.map(airport, AirportResponseDto.class);
    }
    
    @Transactional(readOnly = true)
    public AirportResponseDto getAirportByCode(String code) {
        Airport airport = airportRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Airport", "code", code));
        
        return modelMapper.map(airport, AirportResponseDto.class);
    }
    
    @Transactional(readOnly = true)
    public List<AirportResponseDto> getAllAirports() {
        return airportRepository.findAll().stream()
                .map(airport -> modelMapper.map(airport, AirportResponseDto.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AirportResponseDto> getAirportsByCity(String city) {
        return airportRepository.findByCity(city).stream()
                .map(airport -> modelMapper.map(airport, AirportResponseDto.class))
                .collect(Collectors.toList());
    }
    
    @Transactional
    public AirportResponseDto updateAirport(Long id, AirportRequestDto requestDto) {
        Airport airport = airportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Airport", id));
        
        modelMapper.map(requestDto, airport);
        airport.setCode(airport.getCode().toUpperCase());
        Airport updatedAirport = airportRepository.save(airport);
        
        return modelMapper.map(updatedAirport, AirportResponseDto.class);
    }
    
    @Transactional
    public void deleteAirport(Long id) {
        if (!airportRepository.existsById(id)) {
            throw new ResourceNotFoundException("Airport", id);
        }
        airportRepository.deleteById(id);
    }
}

