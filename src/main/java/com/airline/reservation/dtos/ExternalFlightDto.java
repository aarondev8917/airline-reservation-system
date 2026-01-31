package com.airline.reservation.dtos;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO representing a flight from a third-party API (e.g. Aviationstack).
 * Used for unified search and import-into-internal flow.
 */
@Data
public class ExternalFlightDto {

    private String id;
    private String flightNumber;
    private String airline;
    private String origin;
    private String destination;
    private Double price;
    /** Departure time (when available from external API). */
    private LocalDateTime departureTime;
    /** Arrival time (when available from external API). */
    private LocalDateTime arrivalTime;
}

