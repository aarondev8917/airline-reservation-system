package com.airline.reservation.controllers;

import com.airline.reservation.dtos.AirportRequestDto;
import com.airline.reservation.dtos.AirportResponseDto;
import com.airline.reservation.dtos.ApiResponse;
import com.airline.reservation.services.AirportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Airport operations
 */
@RestController
@RequestMapping("/api/airports")
@RequiredArgsConstructor
public class AirportController {
    
    private final AirportService airportService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<AirportResponseDto>> createAirport(
            @Valid @RequestBody AirportRequestDto requestDto) {
        AirportResponseDto response = airportService.createAirport(requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Airport created successfully", response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AirportResponseDto>> getAirportById(@PathVariable Long id) {
        AirportResponseDto response = airportService.getAirportById(id);
        return ResponseEntity.ok(ApiResponse.success("Airport retrieved successfully", response));
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<AirportResponseDto>> getAirportByCode(@PathVariable String code) {
        AirportResponseDto response = airportService.getAirportByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Airport retrieved successfully", response));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<AirportResponseDto>>> getAllAirports() {
        List<AirportResponseDto> response = airportService.getAllAirports();
        return ResponseEntity.ok(ApiResponse.success("Airports retrieved successfully", response));
    }
    
    @GetMapping("/city/{city}")
    public ResponseEntity<ApiResponse<List<AirportResponseDto>>> getAirportsByCity(@PathVariable String city) {
        List<AirportResponseDto> response = airportService.getAirportsByCity(city);
        return ResponseEntity.ok(ApiResponse.success("Airports retrieved successfully", response));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AirportResponseDto>> updateAirport(
            @PathVariable Long id,
            @Valid @RequestBody AirportRequestDto requestDto) {
        AirportResponseDto response = airportService.updateAirport(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success("Airport updated successfully", response));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAirport(@PathVariable Long id) {
        airportService.deleteAirport(id);
        return ResponseEntity.ok(ApiResponse.success("Airport deleted successfully", null));
    }
}

