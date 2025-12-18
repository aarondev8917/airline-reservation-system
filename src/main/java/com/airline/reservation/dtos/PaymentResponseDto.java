package com.airline.reservation.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Payment responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {
    
    private Long id;
    private String transactionId;
    private Double amount;
    private String paymentMethod;
    private String status;
    private LocalDateTime paymentDate;
    private String bookingReference;
}

