package com.airline.reservation.exceptions;

/**
 * Exception thrown when a payment operation fails
 */
public class PaymentFailedException extends RuntimeException {
    
    public PaymentFailedException(String message) {
        super(message);
    }
}

