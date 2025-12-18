package com.airline.reservation.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for Flight search requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchRequestDto {
    
    @NotBlank(message = "Departure airport code is required")
    private String departureAirportCode;
    
    @NotBlank(message = "Arrival airport code is required")
    private String arrivalAirportCode;
    
    @NotNull(message = "Departure date is required")
    private LocalDate departureDate;
}

