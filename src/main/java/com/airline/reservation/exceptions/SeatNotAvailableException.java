package com.airline.reservation.exceptions;

/**
 * Exception thrown when a seat is not available for booking
 */
public class SeatNotAvailableException extends RuntimeException {
    
    public SeatNotAvailableException(String message) {
        super(message);
    }
}

