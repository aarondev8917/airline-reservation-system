package com.airline.reservation.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for Passenger responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassengerResponseDto {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String passportNumber;
    private String nationality;
    private LocalDateTime createdAt;
}

