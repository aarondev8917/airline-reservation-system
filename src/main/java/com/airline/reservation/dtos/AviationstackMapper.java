package com.airline.reservation.dtos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Maps Aviationstack API DTOs to our external flight representation.
 * Lives in dtos package to access package-private Aviationstack types.
 */
public final class AviationstackMapper {

    private AviationstackMapper() {}

    public static ExternalFlightDto toExternalFlightDto(AviationstackFlightDto a) {
        ExternalFlightDto dto = new ExternalFlightDto();

        if (a.getFlight() != null) {
            dto.setFlightNumber(a.getFlight().getNumber() != null ? a.getFlight().getNumber() : a.getFlight().getIata());
            dto.setId(a.getFlight().getIata() != null ? a.getFlight().getIata() : a.getFlight().getNumber());
        }
        if (a.getAirline() != null) {
            dto.setAirline(a.getAirline().getName());
        }
        if (a.getDeparture() != null && a.getDeparture().getAirport() != null) {
            String o = a.getDeparture().getIata() != null ? a.getDeparture().getIata() : a.getDeparture().getAirport().getIata();
            dto.setOrigin(o);
        }
        if (a.getArrival() != null && a.getArrival().getAirport() != null) {
            String dest = a.getArrival().getIata() != null ? a.getArrival().getIata() : a.getArrival().getAirport().getIata();
            dto.setDestination(dest);
        }

        dto.setPrice(estimatePrice(dto.getOrigin(), dto.getDestination()));

        if (a.getDeparture() != null && a.getDeparture().getScheduled() != null) {
            dto.setDepartureTime(parseIso(a.getDeparture().getScheduled()));
        }
        if (a.getArrival() != null && a.getArrival().getScheduled() != null) {
            dto.setArrivalTime(parseIso(a.getArrival().getScheduled()));
        }

        return dto;
    }

    private static double estimatePrice(String origin, String destination) {
        if (origin != null && destination != null) {
            boolean domestic = origin.length() == 3 && destination.length() == 3;
            return domestic ? 299.99 : 599.99;
        }
        return 399.99;
    }

    private static LocalDateTime parseIso(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            String n = s.length() > 19 ? s.substring(0, 19) : s;
            return LocalDateTime.parse(n, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
