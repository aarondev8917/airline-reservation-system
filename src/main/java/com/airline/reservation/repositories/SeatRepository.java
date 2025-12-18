package com.airline.reservation.repositories;

import com.airline.reservation.models.Flight;
import com.airline.reservation.models.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Seat entity
 */
@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    List<Seat> findByFlight(Flight flight);
    
    List<Seat> findByFlightAndStatus(Flight flight, Seat.SeatStatus status);
    
    Optional<Seat> findByFlightAndSeatNumber(Flight flight, String seatNumber);
    
    List<Seat> findByFlightAndSeatClass(Flight flight, Seat.SeatClass seatClass);
    
    long countByFlightAndStatus(Flight flight, Seat.SeatStatus status);
}

