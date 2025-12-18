package com.airline.reservation.repositories;

import com.airline.reservation.models.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Passenger entity
 */
@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    
    Optional<Passenger> findByEmail(String email);
    
    Optional<Passenger> findByPassportNumber(String passportNumber);
    
    boolean existsByEmail(String email);
    
    boolean existsByPassportNumber(String passportNumber);
}

