package com.airline.reservation.controllers;

import com.airline.reservation.dtos.ApiResponse;
import com.airline.reservation.dtos.SeatResponseDto;
import com.airline.reservation.services.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Seat operations
 */
@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {
    
    private final SeatService seatService;
    
    @GetMapping("/flight/{flightId}")
    public ResponseEntity<ApiResponse<List<SeatResponseDto>>> getSeatsByFlightId(
            @PathVariable Long flightId) {
        List<SeatResponseDto> response = seatService.getSeatsByFlightId(flightId);
        return ResponseEntity.ok(ApiResponse.success("Seats retrieved successfully", response));
    }
    
    @GetMapping("/flight/{flightId}/available")
    public ResponseEntity<ApiResponse<List<SeatResponseDto>>> getAvailableSeatsByFlightId(
            @PathVariable Long flightId) {
        List<SeatResponseDto> response = seatService.getAvailableSeatsByFlightId(flightId);
        return ResponseEntity.ok(ApiResponse.success("Available seats retrieved successfully", response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SeatResponseDto>> getSeatById(@PathVariable Long id) {
        SeatResponseDto response = seatService.getSeatById(id);
        return ResponseEntity.ok(ApiResponse.success("Seat retrieved successfully", response));
    }
}

