package com.airline.reservation.controllers;

import com.airline.reservation.dtos.ApiResponse;
import com.airline.reservation.dtos.ExternalFlightDto;
import com.airline.reservation.dtos.FlightRequestDto;
import com.airline.reservation.dtos.FlightResponseDto;
import com.airline.reservation.dtos.FlightSearchRequestDto;
import com.airline.reservation.dtos.ImportExternalFlightRequestDto;
import com.airline.reservation.dtos.UnifiedFlightDto;
import com.airline.reservation.services.ExternalFlightApiService;
import com.airline.reservation.services.FlightSearchService;
import com.airline.reservation.services.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Flight operations
 */
@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;
    private final ExternalFlightApiService externalFlightApiService;
    private final FlightSearchService flightSearchService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<FlightResponseDto>> createFlight(
            @Valid @RequestBody FlightRequestDto requestDto) {
        FlightResponseDto response = flightService.createFlight(requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Flight created successfully", response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FlightResponseDto>> getFlightById(@PathVariable Long id) {
        FlightResponseDto response = flightService.getFlightById(id);
        return ResponseEntity.ok(ApiResponse.success("Flight retrieved successfully", response));
    }
    
    @GetMapping("/number/{flightNumber}")
    public ResponseEntity<ApiResponse<FlightResponseDto>> getFlightByFlightNumber(
            @PathVariable String flightNumber) {
        FlightResponseDto response = flightService.getFlightByFlightNumber(flightNumber);
        return ResponseEntity.ok(ApiResponse.success("Flight retrieved successfully", response));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<FlightResponseDto>>> getAllFlights() {
        List<FlightResponseDto> response = flightService.getAllFlights();
        return ResponseEntity.ok(ApiResponse.success("Flights retrieved successfully", response));
    }

    /**
     * Fetch flights from Aviationstack API (or mock data if API key not configured).
     * Returns real-time flight data from Aviationstack when API key is provided.
     */
    @GetMapping("/external")
    public ResponseEntity<ApiResponse<List<ExternalFlightDto>>> getExternalFlights() {
        List<ExternalFlightDto> flights = externalFlightApiService.fetchAllExternalFlights();
        return ResponseEntity.ok(ApiResponse.success("External flights retrieved successfully", flights));
    }

    /**
     * Fetch external flights by flight number (IATA code) from Aviationstack API.
     * Example: GET /api/flights/external/number/AA101
     */
    @GetMapping("/external/number/{flightNumber}")
    public ResponseEntity<ApiResponse<List<ExternalFlightDto>>> getExternalFlightsByFlightNumber(
            @PathVariable String flightNumber) {
        List<ExternalFlightDto> flights = externalFlightApiService.fetchFlightsByFlightNumber(flightNumber);
        return ResponseEntity.ok(ApiResponse.success(
                String.format("External flights for %s retrieved successfully", flightNumber), flights));
    }

    /**
     * Fetch a single external flight by its identifier (flight number/IATA code).
     * Uses Aviationstack API when configured, otherwise returns mock data.
     */
    @GetMapping("/external/{externalId}")
    public ResponseEntity<ApiResponse<ExternalFlightDto>> getExternalFlightById(@PathVariable String externalId) {
        ExternalFlightDto flight = externalFlightApiService.fetchExternalFlightById(externalId);
        return ResponseEntity.ok(ApiResponse.success("External flight retrieved successfully", flight));
    }
    
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<FlightResponseDto>>> searchFlights(
            @Valid @RequestBody FlightSearchRequestDto searchDto) {
        List<FlightResponseDto> response = flightService.searchFlights(searchDto);
        return ResponseEntity.ok(ApiResponse.success("Flights retrieved successfully", response));
    }

    /**
     * Unified search: internal DB + optional Aviationstack results.
     * Use {@code includeExternal=true} to include external flights. Each result has {@code source} and {@code bookable}.
     */
    @PostMapping("/search-unified")
    public ResponseEntity<ApiResponse<List<UnifiedFlightDto>>> searchUnified(
            @Valid @RequestBody FlightSearchRequestDto searchDto,
            @RequestParam(name = "includeExternal", defaultValue = "true") boolean includeExternal) {
        List<UnifiedFlightDto> results = flightSearchService.searchUnified(searchDto, includeExternal);
        return ResponseEntity.ok(ApiResponse.success("Flights retrieved successfully", results));
    }

    /**
     * Import an external (Aviationstack) flight into the internal system.
     * Provide {@code externalFlightId} (flight number) to fetch from API, or {@code externalFlight} for direct import.
     * The created flight can then be used for booking.
     */
    @PostMapping("/import-from-external")
    public ResponseEntity<ApiResponse<FlightResponseDto>> importFromExternal(
            @RequestBody ImportExternalFlightRequestDto request) {
        ExternalFlightDto toImport;
        if (request.getExternalFlight() != null) {
            toImport = request.getExternalFlight();
        } else if (request.getExternalFlightId() != null && !request.getExternalFlightId().isBlank()) {
            toImport = externalFlightApiService.fetchExternalFlightById(request.getExternalFlightId());
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<FlightResponseDto>error("Provide externalFlightId or externalFlight"));
        }
        FlightResponseDto created = flightService.importExternalFlight(toImport);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("External flight imported successfully", created));
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<FlightResponseDto>> updateFlightStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        FlightResponseDto response = flightService.updateFlightStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Flight status updated successfully", response));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return ResponseEntity.ok(ApiResponse.success("Flight deleted successfully", null));
    }
}

