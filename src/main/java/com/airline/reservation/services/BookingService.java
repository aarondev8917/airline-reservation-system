package com.airline.reservation.services;

import com.airline.reservation.dtos.*;
import com.airline.reservation.exceptions.InvalidBookingException;
import com.airline.reservation.exceptions.ResourceNotFoundException;
import com.airline.reservation.exceptions.SeatNotAvailableException;
import com.airline.reservation.models.*;
import com.airline.reservation.repositories.*;
import com.airline.reservation.configs.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for Booking operations
 */
@Service
@RequiredArgsConstructor
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final FlightRepository flightRepository;
    private final SeatRepository seatRepository;
    private final ModelMapper modelMapper;
    
    @Transactional
    @CacheEvict(cacheNames = {CacheConfig.CACHE_SEATS, CacheConfig.CACHE_FLIGHTS}, allEntries = true)
    public BookingResponseDto createBooking(BookingRequestDto requestDto) {
        // Validate passenger
        Passenger passenger = passengerRepository.findById(requestDto.getPassengerId())
                .orElseThrow(() -> new ResourceNotFoundException("Passenger", requestDto.getPassengerId()));
        
        // Validate flight
        Flight flight = flightRepository.findById(requestDto.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight", requestDto.getFlightId()));
        
        // Validate seat
        Seat seat = seatRepository.findById(requestDto.getSeatId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat", requestDto.getSeatId()));
        
        // Check if seat belongs to the flight
        if (!seat.getFlight().getId().equals(flight.getId())) {
            throw new InvalidBookingException("Selected seat does not belong to the chosen flight");
        }
        
        // Check seat availability
        if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
            throw new SeatNotAvailableException(seat.getSeatNumber());
        }
        
        // Check flight availability
        if (flight.getAvailableSeats() <= 0) {
            throw new InvalidBookingException("No seats available on this flight");
        }
        
        // Check flight status
        if (flight.getStatus() != Flight.FlightStatus.SCHEDULED) {
            throw new InvalidBookingException("Flight is not available for booking. Status: " + flight.getStatus());
        }
        
        // Create booking
        Booking booking = new Booking();
        booking.setBookingReference(generateBookingReference());
        booking.setPassenger(passenger);
        booking.setFlight(flight);
        booking.setSeat(seat);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setTotalPrice(flight.getBasePrice() + seat.getPrice());
        
        // Update seat status
        seat.setStatus(Seat.SeatStatus.RESERVED);
        seatRepository.save(seat);
        
        // Update available seats
        flight.setAvailableSeats(flight.getAvailableSeats() - 1);
        flightRepository.save(flight);
        
        Booking savedBooking = bookingRepository.save(booking);
        
        return convertToResponseDto(savedBooking);
    }
    
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        
        return convertToResponseDto(booking);
    }
    
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingByReference(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "bookingReference", bookingReference));
        
        return convertToResponseDto(booking);
    }
    
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByPassengerId(Long passengerId) {
        Passenger passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger", passengerId));
        
        return bookingRepository.findByPassenger(passenger).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    @CacheEvict(cacheNames = {CacheConfig.CACHE_SEATS, CacheConfig.CACHE_FLIGHTS}, allEntries = true)
    public BookingResponseDto confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new InvalidBookingException("Only pending bookings can be confirmed");
        }
        
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        
        // Update seat status to occupied
        Seat seat = booking.getSeat();
        seat.setStatus(Seat.SeatStatus.OCCUPIED);
        seatRepository.save(seat);
        
        Booking updatedBooking = bookingRepository.save(booking);
        
        return convertToResponseDto(updatedBooking);
    }
    
    @Transactional
    @CacheEvict(cacheNames = {CacheConfig.CACHE_SEATS, CacheConfig.CACHE_FLIGHTS}, allEntries = true)
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new InvalidBookingException("Booking is already cancelled");
        }
        
        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new InvalidBookingException("Cannot cancel completed booking");
        }
        
        // Update booking status
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        
        // Free up the seat
        Seat seat = booking.getSeat();
        seat.setStatus(Seat.SeatStatus.AVAILABLE);
        seatRepository.save(seat);
        
        // Update available seats
        Flight flight = booking.getFlight();
        flight.setAvailableSeats(flight.getAvailableSeats() + 1);
        flightRepository.save(flight);
        
        bookingRepository.save(booking);
    }
    
    private String generateBookingReference() {
        return "BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private BookingResponseDto convertToResponseDto(Booking booking) {
        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(booking.getId());
        responseDto.setBookingReference(booking.getBookingReference());
        responseDto.setPassenger(modelMapper.map(booking.getPassenger(), PassengerResponseDto.class));
        
        FlightResponseDto flightDto = new FlightResponseDto();
        flightDto.setId(booking.getFlight().getId());
        flightDto.setFlightNumber(booking.getFlight().getFlightNumber());
        flightDto.setAirlineName(booking.getFlight().getAirlineName());
        flightDto.setDepartureAirport(modelMapper.map(booking.getFlight().getDepartureAirport(), AirportResponseDto.class));
        flightDto.setArrivalAirport(modelMapper.map(booking.getFlight().getArrivalAirport(), AirportResponseDto.class));
        flightDto.setDepartureTime(booking.getFlight().getDepartureTime());
        flightDto.setArrivalTime(booking.getFlight().getArrivalTime());
        responseDto.setFlight(flightDto);
        
        responseDto.setSeat(modelMapper.map(booking.getSeat(), SeatResponseDto.class));
        responseDto.setStatus(booking.getStatus().toString());
        responseDto.setTotalPrice(booking.getTotalPrice());
        responseDto.setCreatedAt(booking.getCreatedAt());
        
        return responseDto;
    }
}

