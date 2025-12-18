package com.airline.reservation.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Booking creation requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {
    
    @NotNull(message = "Passenger ID is required")
    private Long passengerId;
    
    @NotNull(message = "Flight ID is required")
    private Long flightId;
    
    @NotNull(message = "Seat ID is required")
    private Long seatId;
}

