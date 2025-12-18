package com.airline.reservation.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Airport Entity representing airports
 */
@Entity
@Table(name = "airports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Airport extends BaseModel {
    
    @NotBlank(message = "Airport code is required")
    @Column(nullable = false, unique = true, length = 3)
    private String code; // IATA code (e.g., JFK, LAX, ORD)
    
    @NotBlank(message = "Airport name is required")
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "City is required")
    @Column(nullable = false)
    private String city;
    
    @NotBlank(message = "Country is required")
    @Column(nullable = false)
    private String country;
    
    @OneToMany(mappedBy = "departureAirport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Flight> departingFlights = new ArrayList<>();
    
    @OneToMany(mappedBy = "arrivalAirport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Flight> arrivingFlights = new ArrayList<>();
}

