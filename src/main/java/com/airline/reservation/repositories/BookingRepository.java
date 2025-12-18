package com.airline.reservation.repositories;

import com.airline.reservation.models.Booking;
import com.airline.reservation.models.Flight;
import com.airline.reservation.models.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Booking entity
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    Optional<Booking> findByBookingReference(String bookingReference);
    
    List<Booking> findByPassenger(Passenger passenger);
    
    List<Booking> findByFlight(Flight flight);
    
    List<Booking> findByPassengerAndStatus(Passenger passenger, Booking.BookingStatus status);
    
    List<Booking> findByStatus(Booking.BookingStatus status);
    
    boolean existsByBookingReference(String bookingReference);
}

