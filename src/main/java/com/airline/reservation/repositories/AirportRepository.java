package com.airline.reservation.repositories;

import com.airline.reservation.models.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository interface for Airport entity
 */
@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {
    
    Optional<Airport> findByCode(String code);
    
    List<Airport> findByCity(String city);
    
    List<Airport> findByCountry(String country);
    
    boolean existsByCode(String code);
}

