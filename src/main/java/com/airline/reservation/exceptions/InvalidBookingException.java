package com.airline.reservation.exceptions;

/**
 * Exception thrown when a booking operation is invalid
 */
public class InvalidBookingException extends RuntimeException {
    
    public InvalidBookingException(String message) {
        super(message);
    }
}

