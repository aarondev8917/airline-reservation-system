package com.airline.reservation.repositories;

import com.airline.reservation.models.Booking;
import com.airline.reservation.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository interface for Payment entity
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    Optional<Payment> findByBooking(Booking booking);
    
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    boolean existsByTransactionId(String transactionId);
}

