package com.airline.reservation.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Flight Entity representing flight schedules
 */
@Entity
@Table(name = "flights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Flight extends BaseModel {
    
    @NotBlank(message = "Flight number is required")
    @Column(nullable = false, unique = true)
    private String flightNumber;
    
    @NotBlank(message = "Airline name is required")
    @Column(nullable = false)
    private String airlineName;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "departure_airport_id", nullable = false)
    private Airport departureAirport;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "arrival_airport_id", nullable = false)
    private Airport arrivalAirport;
    
    @NotNull(message = "Departure time is required")
    @Column(nullable = false)
    private LocalDateTime departureTime;
    
    @NotNull(message = "Arrival time is required")
    @Column(nullable = false)
    private LocalDateTime arrivalTime;
    
    @Positive(message = "Total seats must be positive")
    @Column(nullable = false)
    private Integer totalSeats;
    
    @Column(nullable = false)
    private Integer availableSeats;
    
    @Positive(message = "Price must be positive")
    @Column(nullable = false)
    private Double basePrice;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightStatus status;
    
    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Seat> seats = new ArrayList<>();
    
    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();
    
    public enum FlightStatus {
        SCHEDULED,
        DELAYED,
        CANCELLED,
        BOARDING,
        DEPARTED,
        ARRIVED
    }
}

