package com.airline.reservation.repositories;

import com.airline.reservation.models.Airport;
import com.airline.reservation.models.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Flight entity
 */
@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    
    Optional<Flight> findByFlightNumber(String flightNumber);
    
    List<Flight> findByDepartureAirportAndArrivalAirport(Airport departureAirport, Airport arrivalAirport);
    
    List<Flight> findByDepartureAirportAndArrivalAirportAndDepartureTimeBetween(
        Airport departureAirport, 
        Airport arrivalAirport, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );
    
    @Query("SELECT f FROM Flight f WHERE f.departureAirport.code = :departureCode " +
           "AND f.arrivalAirport.code = :arrivalCode " +
           "AND f.departureTime >= :departureDate " +
           "AND f.availableSeats > 0 " +
           "AND f.status = 'SCHEDULED'")
    List<Flight> searchAvailableFlights(
        @Param("departureCode") String departureCode,
        @Param("arrivalCode") String arrivalCode,
        @Param("departureDate") LocalDateTime departureDate
    );
    
    List<Flight> findByStatus(Flight.FlightStatus status);
}

