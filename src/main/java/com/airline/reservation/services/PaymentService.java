package com.airline.reservation.services;

import com.airline.reservation.dtos.PaymentRequestDto;
import com.airline.reservation.dtos.PaymentResponseDto;
import com.airline.reservation.exceptions.InvalidBookingException;
import com.airline.reservation.exceptions.PaymentFailedException;
import com.airline.reservation.exceptions.ResourceNotFoundException;
import com.airline.reservation.models.Booking;
import com.airline.reservation.models.Payment;
import com.airline.reservation.repositories.BookingRepository;
import com.airline.reservation.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for Payment operations
 */
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto requestDto) {
        // Validate booking
        Booking booking = bookingRepository.findById(requestDto.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", requestDto.getBookingId()));
        
        // Check booking status
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new InvalidBookingException("Cannot process payment for booking with status: " + booking.getStatus());
        }
        
        // Check if payment already exists
        if (paymentRepository.findByBooking(booking).isPresent()) {
            throw new InvalidBookingException("Payment already processed for this booking");
        }
        
        // Create payment
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setTransactionId(generateTransactionId());
        payment.setAmount(booking.getTotalPrice());
        payment.setPaymentMethod(Payment.PaymentMethod.valueOf(requestDto.getPaymentMethod().toUpperCase()));
        payment.setPaymentDate(LocalDateTime.now());
        
        // Simulate payment processing
        boolean paymentSuccess = processPaymentGateway(payment);
        
        if (paymentSuccess) {
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            
            // Update booking status to confirmed
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            throw new PaymentFailedException("Payment processing failed");
        }
        
        Payment savedPayment = paymentRepository.save(payment);
        
        return convertToResponseDto(savedPayment);
    }
    
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
        
        return convertToResponseDto(payment);
    }
    
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByTransactionId(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionId", transactionId));
        
        return convertToResponseDto(payment);
    }
    
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByBookingId(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        
        Payment payment = paymentRepository.findByBooking(booking)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for booking ID: " + bookingId));
        
        return convertToResponseDto(payment);
    }
    
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    private boolean processPaymentGateway(Payment payment) {
        // Simulate payment gateway processing
        // In a real application, this would integrate with actual payment gateways
        // For now, we'll simulate a 95% success rate
        return Math.random() > 0.05;
    }
    
    private String generateTransactionId() {
        return "TXN" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
    
    private PaymentResponseDto convertToResponseDto(Payment payment) {
        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setId(payment.getId());
        responseDto.setTransactionId(payment.getTransactionId());
        responseDto.setAmount(payment.getAmount());
        responseDto.setPaymentMethod(payment.getPaymentMethod().toString());
        responseDto.setStatus(payment.getStatus().toString());
        responseDto.setPaymentDate(payment.getPaymentDate());
        responseDto.setBookingReference(payment.getBooking().getBookingReference());
        return responseDto;
    }
}

