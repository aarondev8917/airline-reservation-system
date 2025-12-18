package com.airline.reservation.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Airport responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirportResponseDto {
    
    private Long id;
    private String code;
    private String name;
    private String city;
    private String country;
}

