package com.airline.reservation.controllers;

import com.airline.reservation.dtos.ApiResponse;
import com.airline.reservation.dtos.PaymentRequestDto;
import com.airline.reservation.dtos.PaymentResponseDto;
import com.airline.reservation.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Payment operations
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponseDto>> processPayment(
            @Valid @RequestBody PaymentRequestDto requestDto) {
        PaymentResponseDto response = paymentService.processPayment(requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment processed successfully", response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> getPaymentById(@PathVariable Long id) {
        PaymentResponseDto response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", response));
    }
    
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> getPaymentByTransactionId(
            @PathVariable String transactionId) {
        PaymentResponseDto response = paymentService.getPaymentByTransactionId(transactionId);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", response));
    }
    
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> getPaymentByBookingId(
            @PathVariable Long bookingId) {
        PaymentResponseDto response = paymentService.getPaymentByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", response));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponseDto>>> getAllPayments() {
        List<PaymentResponseDto> response = paymentService.getAllPayments();
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", response));
    }
}

