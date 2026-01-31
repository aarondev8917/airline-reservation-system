package com.airline.reservation.dtos;

import lombok.Data;

/**
 * Request to import an external (Aviationstack) flight into the internal system.
 * Provide either {@code externalFlightId} (flight number) to fetch from API, or {@code externalFlight} for direct import.
 */
@Data
public class ImportExternalFlightRequestDto {

    /** External flight ID / flight number (IATA). Fetched from Aviationstack when provided. */
    private String externalFlightId;

    /** Pre-fetched external flight. Used as-is when provided; ignores {@code externalFlightId}. */
    private ExternalFlightDto externalFlight;
}
