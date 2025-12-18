package com.airline.reservation.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Flight creation/update requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightRequestDto {
    
    @NotBlank(message = "Flight number is required")
    private String flightNumber;
    
    @NotBlank(message = "Airline name is required")
    private String airlineName;
    
    @NotNull(message = "Departure airport ID is required")
    private Long departureAirportId;
    
    @NotNull(message = "Arrival airport ID is required")
    private Long arrivalAirportId;
    
    @NotNull(message = "Departure time is required")
    @Future(message = "Departure time must be in the future")
    private LocalDateTime departureTime;
    
    @NotNull(message = "Arrival time is required")
    @Future(message = "Arrival time must be in the future")
    private LocalDateTime arrivalTime;
    
    @NotNull(message = "Total seats is required")
    @Positive(message = "Total seats must be positive")
    private Integer totalSeats;
    
    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private Double basePrice;
}

