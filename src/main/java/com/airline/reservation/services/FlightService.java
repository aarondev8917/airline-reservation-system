package com.airline.reservation.services;

import com.airline.reservation.dtos.AirportResponseDto;
import com.airline.reservation.dtos.ExternalFlightDto;
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
import com.airline.reservation.configs.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    @CacheEvict(cacheNames = {CacheConfig.CACHE_FLIGHTS, CacheConfig.CACHE_SEATS}, allEntries = true)
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
    @Cacheable(cacheNames = CacheConfig.CACHE_FLIGHTS, key = "#id")
    public FlightResponseDto getFlightById(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", id));
        
        return convertToResponseDto(flight);
    }
    
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CACHE_FLIGHTS, key = "'number:' + #flightNumber")
    public FlightResponseDto getFlightByFlightNumber(String flightNumber) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "flightNumber", flightNumber));
        
        return convertToResponseDto(flight);
    }
    
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CACHE_FLIGHTS, key = "#searchDto.departureAirportCode + '-' + #searchDto.arrivalAirportCode + '-' + #searchDto.departureDate")
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
    @Cacheable(cacheNames = CacheConfig.CACHE_FLIGHTS, key = "'all'")
    public List<FlightResponseDto> getAllFlights() {
        return flightRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CACHE_FLIGHTS, allEntries = true)
    public FlightResponseDto updateFlightStatus(Long id, String status) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", id));
        
        flight.setStatus(Flight.FlightStatus.valueOf(status.toUpperCase()));
        Flight updatedFlight = flightRepository.save(flight);
        
        return convertToResponseDto(updatedFlight);
    }
    
    @Transactional
    @CacheEvict(cacheNames = {CacheConfig.CACHE_FLIGHTS, CacheConfig.CACHE_SEATS}, allEntries = true)
    public void deleteFlight(Long id) {
        if (!flightRepository.existsById(id)) {
            throw new ResourceNotFoundException("Flight", id);
        }
        flightRepository.deleteById(id);
    }

    /**
     * Import an external (Aviationstack) flight into the internal system.
     * Creates placeholder airports by IATA if they don't exist, then creates the flight and seats.
     * The resulting flight can be used for booking like any internal flight.
     */
    @Transactional
    @CacheEvict(cacheNames = {CacheConfig.CACHE_FLIGHTS, CacheConfig.CACHE_SEATS, CacheConfig.CACHE_AIRPORTS}, allEntries = true)
    public FlightResponseDto importExternalFlight(ExternalFlightDto external) {
        if (external.getFlightNumber() == null || external.getFlightNumber().isBlank()) {
            throw new InvalidBookingException("External flight number is required");
        }
        if (flightRepository.findByFlightNumber(external.getFlightNumber()).isPresent()) {
            throw new ResourceAlreadyExistsException("Flight", "flightNumber", external.getFlightNumber());
        }

        String depCode = external.getOrigin() != null ? external.getOrigin().trim().toUpperCase() : "XXX";
        String arrCode = external.getDestination() != null ? external.getDestination().trim().toUpperCase() : "YYY";
        if (depCode.equals(arrCode)) {
            throw new InvalidBookingException("Departure and arrival airports cannot be the same");
        }

        Airport dep = getOrCreateAirportByCode(depCode, "Airport " + depCode);
        Airport arr = getOrCreateAirportByCode(arrCode, "Airport " + arrCode);

        LocalDateTime depTime = external.getDepartureTime() != null
                ? external.getDepartureTime()
                : LocalDate.now().atTime(10, 0);
        LocalDateTime arrTime = external.getArrivalTime() != null
                ? external.getArrivalTime()
                : depTime.plusHours(2);
        if (arrTime.isBefore(depTime) || arrTime.equals(depTime)) {
            arrTime = depTime.plusHours(2);
        }

        double price = external.getPrice() != null && external.getPrice() > 0 ? external.getPrice() : 199.99;
        int totalSeats = 120;

        Flight flight = new Flight();
        flight.setFlightNumber(external.getFlightNumber());
        flight.setAirlineName(external.getAirline() != null ? external.getAirline() : "Unknown");
        flight.setDepartureAirport(dep);
        flight.setArrivalAirport(arr);
        flight.setDepartureTime(depTime);
        flight.setArrivalTime(arrTime);
        flight.setTotalSeats(totalSeats);
        flight.setAvailableSeats(totalSeats);
        flight.setBasePrice(price);
        flight.setStatus(Flight.FlightStatus.SCHEDULED);

        Flight saved = flightRepository.save(flight);
        createSeatsForFlight(saved);
        return convertToResponseDto(saved);
    }

    private Airport getOrCreateAirportByCode(String code, String name) {
        return airportRepository.findByCode(code)
                .orElseGet(() -> {
                    Airport a = new Airport();
                    a.setCode(code);
                    a.setName(name);
                    a.setCity("-");
                    a.setCountry("-");
                    return airportRepository.save(a);
                });
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

