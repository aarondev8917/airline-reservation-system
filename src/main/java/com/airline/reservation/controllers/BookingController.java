package com.airline.reservation.controllers;

import com.airline.reservation.dtos.ApiResponse;
import com.airline.reservation.dtos.BookingRequestDto;
import com.airline.reservation.dtos.BookingResponseDto;
import com.airline.reservation.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Booking operations
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    
    private final BookingService bookingService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponseDto>> createBooking(
            @Valid @RequestBody BookingRequestDto requestDto) {
        BookingResponseDto response = bookingService.createBooking(requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponseDto>> getBookingById(@PathVariable Long id) {
        BookingResponseDto response = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", response));
    }
    
    @GetMapping("/reference/{bookingReference}")
    public ResponseEntity<ApiResponse<BookingResponseDto>> getBookingByReference(
            @PathVariable String bookingReference) {
        BookingResponseDto response = bookingService.getBookingByReference(bookingReference);
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", response));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponseDto>>> getAllBookings() {
        List<BookingResponseDto> response = bookingService.getAllBookings();
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", response));
    }
    
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<ApiResponse<List<BookingResponseDto>>> getBookingsByPassengerId(
            @PathVariable Long passengerId) {
        List<BookingResponseDto> response = bookingService.getBookingsByPassengerId(passengerId);
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", response));
    }
    
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<BookingResponseDto>> confirmBooking(@PathVariable Long id) {
        BookingResponseDto response = bookingService.confirmBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed successfully", response));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", null));
    }
}

