package com.airline.reservation.services;

import com.airline.reservation.dtos.ExternalFlightDto;
import com.airline.reservation.dtos.FlightResponseDto;
import com.airline.reservation.dtos.FlightSearchRequestDto;
import com.airline.reservation.dtos.UnifiedFlightDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Unified flight search: combines internal (DB) and external (Aviationstack) results.
 * Use {@link UnifiedFlightDto#isBookable()} to know if a flight can be booked.
 */
@Service
@RequiredArgsConstructor
public class FlightSearchService {

    private final FlightService flightService;
    private final ExternalFlightApiService externalFlightApiService;

    /**
     * Search flights by route and optional date. Optionally include external (Aviationstack) results.
     *
     * @param searchDto       departure/arrival codes and departure date
     * @param includeExternal when true, also fetches from Aviationstack and merges results
     * @return combined list (internal first, then external); each item has {@code source} and {@code bookable}
     */
    public List<UnifiedFlightDto> searchUnified(FlightSearchRequestDto searchDto, boolean includeExternal) {
        List<UnifiedFlightDto> combined = new ArrayList<>();

        List<FlightResponseDto> internal = flightService.searchFlights(searchDto);
        combined.addAll(internal.stream().map(this::toUnified).collect(Collectors.toList()));

        if (includeExternal) {
            String dep = searchDto.getDepartureAirportCode().trim().toUpperCase();
            String arr = searchDto.getArrivalAirportCode().trim().toUpperCase();
            List<ExternalFlightDto> external = externalFlightApiService.fetchFlightsByRoute(dep, arr);
            combined.addAll(external.stream().map(this::toUnified).collect(Collectors.toList()));
        }

        return combined;
    }

    private UnifiedFlightDto toUnified(FlightResponseDto r) {
        UnifiedFlightDto u = new UnifiedFlightDto();
        u.setSource(UnifiedFlightDto.SOURCE_INTERNAL);
        u.setBookable(true);
        u.setInternalFlightId(r.getId());
        u.setId(String.valueOf(r.getId()));
        u.setFlightNumber(r.getFlightNumber());
        u.setAirlineName(r.getAirlineName());
        u.setDepartureTime(r.getDepartureTime());
        u.setArrivalTime(r.getArrivalTime());
        u.setBasePrice(r.getBasePrice());
        u.setTotalSeats(r.getTotalSeats());
        u.setAvailableSeats(r.getAvailableSeats());
        if (r.getDepartureAirport() != null) {
            u.setDepartureAirportCode(r.getDepartureAirport().getCode());
            u.setDepartureAirportName(r.getDepartureAirport().getName());
        }
        if (r.getArrivalAirport() != null) {
            u.setArrivalAirportCode(r.getArrivalAirport().getCode());
            u.setArrivalAirportName(r.getArrivalAirport().getName());
        }
        return u;
    }

    private UnifiedFlightDto toUnified(ExternalFlightDto e) {
        UnifiedFlightDto u = new UnifiedFlightDto();
        u.setSource(UnifiedFlightDto.SOURCE_EXTERNAL);
        u.setBookable(false);
        u.setInternalFlightId(null);
        u.setId(e.getId());
        u.setFlightNumber(e.getFlightNumber());
        u.setAirlineName(e.getAirline());
        u.setDepartureAirportCode(e.getOrigin());
        u.setArrivalAirportCode(e.getDestination());
        u.setDepartureAirportName(e.getOrigin());
        u.setArrivalAirportName(e.getDestination());
        u.setBasePrice(e.getPrice());
        u.setDepartureTime(e.getDepartureTime());
        u.setArrivalTime(e.getArrivalTime());
        u.setTotalSeats(null);
        u.setAvailableSeats(null);
        return u;
    }
}
