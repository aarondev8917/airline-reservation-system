package com.airline.reservation.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Unified flight representation for search results.
 * Can represent either an internal (DB) flight or an external (Aviationstack) flight.
 * Use {@link #isBookable()} to decide if the flight can be booked via the booking API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedFlightDto {

    public static final String SOURCE_INTERNAL = "internal";
    public static final String SOURCE_EXTERNAL = "external";

    /** {@value SOURCE_INTERNAL} or {@value SOURCE_EXTERNAL} */
    private String source;

    /** True only for internal flights; external flights must be imported first. */
    private boolean bookable;

    /** Internal flight ID (only set when {@code source == internal}); use for booking. */
    private Long internalFlightId;

    /** External or internal identifier (flight number or string id). */
    private String id;

    private String flightNumber;
    private String airlineName;
    private String departureAirportCode;
    private String arrivalAirportCode;
    private String departureAirportName;
    private String arrivalAirportName;
    private Double basePrice;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer totalSeats;
    private Integer availableSeats;
}
