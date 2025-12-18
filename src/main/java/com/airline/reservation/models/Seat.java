package com.airline.reservation.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Seat Entity representing seats in a flight
 */
@Entity
@Table(name = "seats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"flight_id", "seat_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seat extends BaseModel {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;
    
    @NotBlank(message = "Seat number is required")
    @Column(nullable = false)
    private String seatNumber; // e.g., "1A", "12C", "30F"
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatClass seatClass;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;
    
    @Column(nullable = false)
    private Double price; // Additional price based on seat class
    
    @OneToOne(mappedBy = "seat", fetch = FetchType.LAZY)
    private Booking booking;
    
    public enum SeatClass {
        ECONOMY,
        PREMIUM_ECONOMY,
        BUSINESS,
        FIRST_CLASS
    }
    
    public enum SeatStatus {
        AVAILABLE,
        RESERVED,
        OCCUPIED,
        BLOCKED
    }
}

