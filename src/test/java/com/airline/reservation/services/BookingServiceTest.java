package com.airline.reservation.services;

import com.airline.reservation.dtos.BookingRequestDto;
import com.airline.reservation.dtos.BookingResponseDto;
import com.airline.reservation.exceptions.InvalidBookingException;
import com.airline.reservation.exceptions.ResourceNotFoundException;
import com.airline.reservation.exceptions.SeatNotAvailableException;
import com.airline.reservation.models.*;
import com.airline.reservation.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("BookingService Unit Tests")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BookingService bookingService;

    private BookingRequestDto bookingRequest;
    private Passenger passenger;
    private Flight flight;
    private Seat seat;
    private Airport departureAirport;
    private Airport arrivalAirport;

    @BeforeEach
    void setUp() {
        // Setup airports
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

        // Setup passenger
        passenger = new Passenger();
        passenger.setId(1L);
        passenger.setFirstName("John");
        passenger.setLastName("Doe");
        passenger.setEmail("john.doe@example.com");

        // Setup flight
        flight = new Flight();
        flight.setId(1L);
        flight.setFlightNumber("AA101");
        flight.setAirlineName("American Airlines");
        flight.setDepartureAirport(departureAirport);
        flight.setArrivalAirport(arrivalAirport);
        flight.setDepartureTime(LocalDateTime.now().plusDays(1));
        flight.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(5));
        flight.setTotalSeats(150);
        flight.setAvailableSeats(100);
        flight.setBasePrice(500.0);
        flight.setStatus(Flight.FlightStatus.SCHEDULED);

        // Setup seat
        seat = new Seat();
        seat.setId(1L);
        seat.setFlight(flight);
        seat.setSeatNumber("1A");
        seat.setSeatClass(Seat.SeatClass.ECONOMY);
        seat.setPrice(500.0);
        seat.setStatus(Seat.SeatStatus.AVAILABLE);

        // Setup booking request
        bookingRequest = new BookingRequestDto();
        bookingRequest.setPassengerId(1L);
        bookingRequest.setFlightId(1L);
        bookingRequest.setSeatId(1L);
    }

    @Test
    @DisplayName("Should successfully create a booking")
    void testCreateBooking_Success() {
        // Given
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(seatRepository.save(any(Seat.class))).thenReturn(seat);
        when(flightRepository.save(any(Flight.class))).thenReturn(flight);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(1L);
            return booking;
        });

        // When
        BookingResponseDto result = bookingService.createBooking(bookingRequest);

        // Then
        assertNotNull(result);
        verify(passengerRepository).findById(1L);
        verify(flightRepository).findById(1L);
        verify(seatRepository).findById(1L);
        verify(seatRepository).save(any(Seat.class));
        verify(flightRepository).save(any(Flight.class));
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw exception when passenger not found")
    void testCreateBooking_PassengerNotFound() {
        // Given
        when(passengerRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.createBooking(bookingRequest);
        });

        verify(passengerRepository).findById(1L);
        verify(flightRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when flight not found")
    void testCreateBooking_FlightNotFound() {
        // Given
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.createBooking(bookingRequest);
        });

        verify(flightRepository).findById(1L);
        verify(seatRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when seat not found")
    void testCreateBooking_SeatNotFound() {
        // Given
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(seatRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.createBooking(bookingRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when seat does not belong to flight")
    void testCreateBooking_SeatNotBelongingToFlight() {
        // Given
        Flight differentFlight = new Flight();
        differentFlight.setId(2L);
        seat.setFlight(differentFlight);

        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));

        // When & Then
        assertThrows(InvalidBookingException.class, () -> {
            bookingService.createBooking(bookingRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when seat is not available")
    void testCreateBooking_SeatNotAvailable() {
        // Given
        seat.setStatus(Seat.SeatStatus.RESERVED);

        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));

        // When & Then
        assertThrows(SeatNotAvailableException.class, () -> {
            bookingService.createBooking(bookingRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when flight has no available seats")
    void testCreateBooking_NoAvailableSeats() {
        // Given
        flight.setAvailableSeats(0);

        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));

        // When & Then
        assertThrows(InvalidBookingException.class, () -> {
            bookingService.createBooking(bookingRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when flight is not scheduled")
    void testCreateBooking_FlightNotScheduled() {
        // Given
        flight.setStatus(Flight.FlightStatus.CANCELLED);

        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));

        // When & Then
        assertThrows(InvalidBookingException.class, () -> {
            bookingService.createBooking(bookingRequest);
        });
    }

    @Test
    @DisplayName("Should successfully get booking by ID")
    void testGetBookingById_Success() {
        // Given
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookingReference("BK12345678");
        booking.setPassenger(passenger);
        booking.setFlight(flight);
        booking.setSeat(seat);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setTotalPrice(1000.0);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // When
        BookingResponseDto result = bookingService.getBookingById(1L);

        // Then
        assertNotNull(result);
        verify(bookingRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when booking not found by ID")
    void testGetBookingById_NotFound() {
        // Given
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.getBookingById(1L);
        });
    }

    @Test
    @DisplayName("Should successfully confirm a pending booking")
    void testConfirmBooking_Success() {
        // Given
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setFlight(flight);
        booking.setPassenger(passenger);
        booking.setSeat(seat);
        seat.setStatus(Seat.SeatStatus.RESERVED);

        BookingResponseDto bookingResponse = new BookingResponseDto();
        bookingResponse.setId(1L);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(seatRepository.save(any(Seat.class))).thenReturn(seat);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        
        // Mock modelMapper for convertToResponseDto
        com.airline.reservation.dtos.PassengerResponseDto passengerDto = new com.airline.reservation.dtos.PassengerResponseDto();
        com.airline.reservation.dtos.SeatResponseDto seatDto = new com.airline.reservation.dtos.SeatResponseDto();
        com.airline.reservation.dtos.AirportResponseDto airportDto = new com.airline.reservation.dtos.AirportResponseDto();
        
        when(modelMapper.map(eq(passenger), eq(com.airline.reservation.dtos.PassengerResponseDto.class))).thenReturn(passengerDto);
        when(modelMapper.map(eq(seat), eq(com.airline.reservation.dtos.SeatResponseDto.class))).thenReturn(seatDto);
        when(modelMapper.map(eq(departureAirport), eq(com.airline.reservation.dtos.AirportResponseDto.class))).thenReturn(airportDto);
        when(modelMapper.map(eq(arrivalAirport), eq(com.airline.reservation.dtos.AirportResponseDto.class))).thenReturn(airportDto);

        // When
        BookingResponseDto result = bookingService.confirmBooking(1L);

        // Then
        assertNotNull(result);
        assertEquals(Booking.BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(Seat.SeatStatus.OCCUPIED, seat.getStatus());
        verify(bookingRepository).save(booking);
        verify(seatRepository).save(seat);
    }

    @Test
    @DisplayName("Should throw exception when confirming non-pending booking")
    void testConfirmBooking_NotPending() {
        // Given
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // When & Then
        assertThrows(InvalidBookingException.class, () -> {
            bookingService.confirmBooking(1L);
        });
    }

    @Test
    @DisplayName("Should successfully cancel a booking")
    void testCancelBooking_Success() {
        // Given
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setSeat(seat);
        booking.setFlight(flight);
        seat.setStatus(Seat.SeatStatus.RESERVED);
        flight.setAvailableSeats(99);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(seatRepository.save(any(Seat.class))).thenReturn(seat);
        when(flightRepository.save(any(Flight.class))).thenReturn(flight);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        // When
        bookingService.cancelBooking(1L);

        // Then
        assertEquals(Booking.BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(Seat.SeatStatus.AVAILABLE, seat.getStatus());
        assertEquals(100, flight.getAvailableSeats());
        verify(bookingRepository).save(booking);
        verify(seatRepository).save(seat);
        verify(flightRepository).save(flight);
    }

    @Test
    @DisplayName("Should throw exception when canceling already cancelled booking")
    void testCancelBooking_AlreadyCancelled() {
        // Given
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(Booking.BookingStatus.CANCELLED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // When & Then
        assertThrows(InvalidBookingException.class, () -> {
            bookingService.cancelBooking(1L);
        });
    }

    @Test
    @DisplayName("Should throw exception when canceling completed booking")
    void testCancelBooking_Completed() {
        // Given
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(Booking.BookingStatus.COMPLETED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // When & Then
        assertThrows(InvalidBookingException.class, () -> {
            bookingService.cancelBooking(1L);
        });
    }
}

