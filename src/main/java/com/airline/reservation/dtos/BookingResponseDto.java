package com.airline.reservation.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Booking responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {
    
    private Long id;
    private String bookingReference;
    private PassengerResponseDto passenger;
    private FlightResponseDto flight;
    private SeatResponseDto seat;
    private String status;
    private Double totalPrice;
    private LocalDateTime createdAt;
}

