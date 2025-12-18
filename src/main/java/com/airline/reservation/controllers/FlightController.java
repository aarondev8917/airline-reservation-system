package com.airline.reservation.controllers;

import com.airline.reservation.dtos.ApiResponse;
import com.airline.reservation.dtos.FlightRequestDto;
import com.airline.reservation.dtos.FlightResponseDto;
import com.airline.reservation.dtos.FlightSearchRequestDto;
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
    
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<FlightResponseDto>>> searchFlights(
            @Valid @RequestBody FlightSearchRequestDto searchDto) {
        List<FlightResponseDto> response = flightService.searchFlights(searchDto);
        return ResponseEntity.ok(ApiResponse.success("Flights retrieved successfully", response));
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

