package com.airline.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Application Class for Airline Reservation System
 * 
 * @author MS Student
 * @version 1.0
 */
@SpringBootApplication
public class AirlineReservationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AirlineReservationSystemApplication.class, args);
        System.out.println("==============================================");
        System.out.println("Airline Reservation System Started Successfully!");
        System.out.println("Server running on: http://localhost:8080");
        System.out.println("==============================================");
    }
}

