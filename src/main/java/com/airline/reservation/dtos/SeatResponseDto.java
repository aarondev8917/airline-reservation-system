package com.airline.reservation.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Seat responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponseDto {
    
    private Long id;
    private String seatNumber;
    private String seatClass;
    private String status;
    private Double price;
}

