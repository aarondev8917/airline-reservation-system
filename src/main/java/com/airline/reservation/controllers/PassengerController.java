package com.airline.reservation.controllers;

import com.airline.reservation.dtos.ApiResponse;
import com.airline.reservation.dtos.PassengerRequestDto;
import com.airline.reservation.dtos.PassengerResponseDto;
import com.airline.reservation.services.PassengerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Passenger operations
 */
@RestController
@RequestMapping("/api/passengers")
@RequiredArgsConstructor
public class PassengerController {
    
    private final PassengerService passengerService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<PassengerResponseDto>> createPassenger(
            @Valid @RequestBody PassengerRequestDto requestDto) {
        PassengerResponseDto response = passengerService.createPassenger(requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Passenger created successfully", response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PassengerResponseDto>> getPassengerById(@PathVariable Long id) {
        PassengerResponseDto response = passengerService.getPassengerById(id);
        return ResponseEntity.ok(ApiResponse.success("Passenger retrieved successfully", response));
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<PassengerResponseDto>> getPassengerByEmail(@PathVariable String email) {
        PassengerResponseDto response = passengerService.getPassengerByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Passenger retrieved successfully", response));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<PassengerResponseDto>>> getAllPassengers() {
        List<PassengerResponseDto> response = passengerService.getAllPassengers();
        return ResponseEntity.ok(ApiResponse.success("Passengers retrieved successfully", response));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PassengerResponseDto>> updatePassenger(
            @PathVariable Long id,
            @Valid @RequestBody PassengerRequestDto requestDto) {
        PassengerResponseDto response = passengerService.updatePassenger(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success("Passenger updated successfully", response));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePassenger(@PathVariable Long id) {
        passengerService.deletePassenger(id);
        return ResponseEntity.ok(ApiResponse.success("Passenger deleted successfully", null));
    }
}

