package com.airline.reservation.services;

import com.airline.reservation.dtos.AirportResponseDto;
import com.airline.reservation.dtos.FlightRequestDto;
import com.airline.reservation.dtos.FlightResponseDto;
import com.airline.reservation.dtos.FlightSearchRequestDto;
import com.airline.reservation.exceptions.InvalidBookingException;
import com.airline.reservation.exceptions.ResourceAlreadyExistsException;
import com.airline.reservation.exceptions.ResourceNotFoundException;
import com.airline.reservation.models.Airport;
import com.airline.reservation.models.Flight;
import com.airline.reservation.models.Seat;
import com.airline.reservation.repositories.AirportRepository;
import com.airline.reservation.repositories.FlightRepository;
import com.airline.reservation.repositories.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Flight operations
 */
@Service
@RequiredArgsConstructor
public class FlightService {
    
    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;
    private final SeatRepository seatRepository;
    private final ModelMapper modelMapper;
    
    @Transactional
    public FlightResponseDto createFlight(FlightRequestDto requestDto) {
        // Check if flight number already exists
        if (flightRepository.findByFlightNumber(requestDto.getFlightNumber()).isPresent()) {
            throw new ResourceAlreadyExistsException("Flight", "flightNumber", requestDto.getFlightNumber());
        }
        
        // Validate departure and arrival airports
        Airport departureAirport = airportRepository.findById(requestDto.getDepartureAirportId())
                .orElseThrow(() -> new ResourceNotFoundException("Airport", requestDto.getDepartureAirportId()));
        
        Airport arrivalAirport = airportRepository.findById(requestDto.getArrivalAirportId())
                .orElseThrow(() -> new ResourceNotFoundException("Airport", requestDto.getArrivalAirportId()));
        
        // Validate that departure and arrival are different
        if (departureAirport.getId().equals(arrivalAirport.getId())) {
            throw new InvalidBookingException("Departure and arrival airports cannot be the same");
        }
        
        // Validate times
        if (requestDto.getArrivalTime().isBefore(requestDto.getDepartureTime())) {
            throw new InvalidBookingException("Arrival time must be after departure time");
        }
        
        Flight flight = new Flight();
        flight.setFlightNumber(requestDto.getFlightNumber());
        flight.setAirlineName(requestDto.getAirlineName());
        flight.setDepartureAirport(departureAirport);
        flight.setArrivalAirport(arrivalAirport);
        flight.setDepartureTime(requestDto.getDepartureTime());
        flight.setArrivalTime(requestDto.getArrivalTime());
        flight.setTotalSeats(requestDto.getTotalSeats());
        flight.setAvailableSeats(requestDto.getTotalSeats());
        flight.setBasePrice(requestDto.getBasePrice());
        flight.setStatus(Flight.FlightStatus.SCHEDULED);
        
        Flight savedFlight = flightRepository.save(flight);
        
        // Create seats for the flight
        createSeatsForFlight(savedFlight);
        
        return convertToResponseDto(savedFlight);
    }
    
    private void createSeatsForFlight(Flight flight) {
        List<Seat> seats = new ArrayList<>();
        int totalSeats = flight.getTotalSeats();
        int seatsPerRow = 6; // Assuming 6 seats per row (A-F)
        
        for (int row = 1; row <= (totalSeats / seatsPerRow); row++) {
            for (char seatLetter = 'A'; seatLetter < 'A' + seatsPerRow; seatLetter++) {
                Seat seat = new Seat();
                seat.setFlight(flight);
                seat.setSeatNumber(row + String.valueOf(seatLetter));
                
                // Assign seat class based on row
                if (row <= 2) {
                    seat.setSeatClass(Seat.SeatClass.FIRST_CLASS);
                    seat.setPrice(flight.getBasePrice() * 3);
                } else if (row <= 5) {
                    seat.setSeatClass(Seat.SeatClass.BUSINESS);
                    seat.setPrice(flight.getBasePrice() * 2);
                } else if (row <= 10) {
                    seat.setSeatClass(Seat.SeatClass.PREMIUM_ECONOMY);
                    seat.setPrice(flight.getBasePrice() * 1.5);
                } else {
                    seat.setSeatClass(Seat.SeatClass.ECONOMY);
                    seat.setPrice(flight.getBasePrice());
                }
                
                seat.setStatus(Seat.SeatStatus.AVAILABLE);
                seats.add(seat);
            }
        }
        
        seatRepository.saveAll(seats);
    }
    
    @Transactional(readOnly = true)
    public FlightResponseDto getFlightById(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", id));
        
        return convertToResponseDto(flight);
    }
    
    @Transactional(readOnly = true)
    public FlightResponseDto getFlightByFlightNumber(String flightNumber) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "flightNumber", flightNumber));
        
        return convertToResponseDto(flight);
    }
    
    @Transactional(readOnly = true)
    public List<FlightResponseDto> searchFlights(FlightSearchRequestDto searchDto) {
        LocalDateTime startOfDay = searchDto.getDepartureDate().atStartOfDay();
        LocalDateTime endOfDay = searchDto.getDepartureDate().atTime(23, 59, 59);
        
        List<Flight> flights = flightRepository.searchAvailableFlights(
                searchDto.getDepartureAirportCode().toUpperCase(),
                searchDto.getArrivalAirportCode().toUpperCase(),
                startOfDay
        );
        
        return flights.stream()
                .filter(f -> f.getDepartureTime().isBefore(endOfDay))
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<FlightResponseDto> getAllFlights() {
        return flightRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public FlightResponseDto updateFlightStatus(Long id, String status) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", id));
        
        flight.setStatus(Flight.FlightStatus.valueOf(status.toUpperCase()));
        Flight updatedFlight = flightRepository.save(flight);
        
        return convertToResponseDto(updatedFlight);
    }
    
    @Transactional
    public void deleteFlight(Long id) {
        if (!flightRepository.existsById(id)) {
            throw new ResourceNotFoundException("Flight", id);
        }
        flightRepository.deleteById(id);
    }
    
    private FlightResponseDto convertToResponseDto(Flight flight) {
        FlightResponseDto responseDto = new FlightResponseDto();
        responseDto.setId(flight.getId());
        responseDto.setFlightNumber(flight.getFlightNumber());
        responseDto.setAirlineName(flight.getAirlineName());
        responseDto.setDepartureAirport(modelMapper.map(flight.getDepartureAirport(), AirportResponseDto.class));
        responseDto.setArrivalAirport(modelMapper.map(flight.getArrivalAirport(), AirportResponseDto.class));
        responseDto.setDepartureTime(flight.getDepartureTime());
        responseDto.setArrivalTime(flight.getArrivalTime());
        responseDto.setTotalSeats(flight.getTotalSeats());
        responseDto.setAvailableSeats(flight.getAvailableSeats());
        responseDto.setBasePrice(flight.getBasePrice());
        responseDto.setStatus(flight.getStatus().toString());
        return responseDto;
    }
}

