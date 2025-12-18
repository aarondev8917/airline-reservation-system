package com.airline.reservation.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Flight responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightResponseDto {
    
    private Long id;
    private String flightNumber;
    private String airlineName;
    private AirportResponseDto departureAirport;
    private AirportResponseDto arrivalAirport;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private Double basePrice;
    private String status;
}

