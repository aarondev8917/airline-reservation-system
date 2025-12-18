package com.airline.reservation.configs;

import com.airline.reservation.models.*;
import com.airline.reservation.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Initializer to populate sample data on application startup
 * Only runs in 'dev' profile to avoid populating production databases
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    private final AirportRepository airportRepository;
    private final PassengerRepository passengerRepository;
    private final FlightRepository flightRepository;
    private final SeatRepository seatRepository;
    
    @Bean
    @Profile("dev") // Only run in dev profile
    public CommandLineRunner initData() {
        return args -> {
            try {
                // Check if data already exists
                if (airportRepository.count() > 0) {
                    log.info("Database already contains data. Skipping initialization.");
                    return;
                }
                
                log.info("==============================================");
                log.info("Initializing database with sample data...");
                log.info("==============================================");
                
                // Create Airports
                List<Airport> airports = createAirports();
                log.info("✓ Created {} airports", airports.size());
                
                // Create Passengers
                List<Passenger> passengers = createPassengers();
                log.info("✓ Created {} passengers", passengers.size());
                
                // Create Flights (seats will be auto-generated)
                List<Flight> flights = createFlights(airports);
                log.info("✓ Created {} flights", flights.size());
                
                log.info("==============================================");
                log.info("DATABASE INITIALIZATION COMPLETE!");
                log.info("Sample Data Available:");
                log.info("  - {} Airports", airports.size());
                log.info("  - {} Passengers", passengers.size());
                log.info("  - {} Flights", flights.size());
                log.info("  - {} Seats (auto-generated)", seatRepository.count());
                log.info("==============================================");
                log.info("Application ready to use!");
                log.info("Test APIs at: http://localhost:8080/api/airports");
                log.info("==============================================");
            } catch (Exception e) {
                log.error("Error initializing database: {}", e.getMessage(), e);
            }
        };
    }
    
    private List<Airport> createAirports() {
        List<Airport> airports = new ArrayList<>();
        
        airports.add(createAirport("DEL", "Indira Gandhi International Airport", "New Delhi", "India"));
        airports.add(createAirport("BOM", "Chhatrapati Shivaji Maharaj International Airport", "Mumbai", "India"));
        airports.add(createAirport("BLR", "Kempegowda International Airport", "Bangalore", "India"));
        airports.add(createAirport("MAA", "Chennai International Airport", "Chennai", "India"));
        airports.add(createAirport("HYD", "Rajiv Gandhi International Airport", "Hyderabad", "India"));
        airports.add(createAirport("CCU", "Netaji Subhas Chandra Bose International Airport", "Kolkata", "India"));
        airports.add(createAirport("JFK", "John F. Kennedy International Airport", "New York", "USA"));
        airports.add(createAirport("LAX", "Los Angeles International Airport", "Los Angeles", "USA"));
        airports.add(createAirport("LHR", "London Heathrow Airport", "London", "UK"));
        airports.add(createAirport("DXB", "Dubai International Airport", "Dubai", "UAE"));
        
        return airportRepository.saveAll(airports);
    }
    
    private Airport createAirport(String code, String name, String city, String country) {
        Airport airport = new Airport();
        airport.setCode(code);
        airport.setName(name);
        airport.setCity(city);
        airport.setCountry(country);
        return airport;
    }
    
    private List<Passenger> createPassengers() {
        List<Passenger> passengers = new ArrayList<>();
        
        passengers.add(createPassenger("Rajesh", "Kumar", "rajesh.kumar@email.com", 
            "9876543210", "1990-05-15", "A1234567", "India"));
        passengers.add(createPassenger("Priya", "Sharma", "priya.sharma@email.com", 
            "9876543211", "1992-08-22", "A1234568", "India"));
        passengers.add(createPassenger("Amit", "Patel", "amit.patel@email.com", 
            "9876543212", "1988-03-10", "A1234569", "India"));
        passengers.add(createPassenger("Sneha", "Reddy", "sneha.reddy@email.com", 
            "9876543213", "1995-11-30", "A1234570", "India"));
        passengers.add(createPassenger("Vikram", "Singh", "vikram.singh@email.com", 
            "9876543214", "1987-07-18", "A1234571", "India"));
        
        return passengerRepository.saveAll(passengers);
    }
    
    private Passenger createPassenger(String firstName, String lastName, String email, 
                                     String phone, String dob, String passport, String nationality) {
        Passenger passenger = new Passenger();
        passenger.setFirstName(firstName);
        passenger.setLastName(lastName);
        passenger.setEmail(email);
        passenger.setPhoneNumber(phone);
        passenger.setDateOfBirth(LocalDate.parse(dob));
        passenger.setPassportNumber(passport);
        passenger.setNationality(nationality);
        return passenger;
    }
    
    private List<Flight> createFlights(List<Airport> airports) {
        List<Flight> flights = new ArrayList<>();
        
        // Domestic flights
        flights.add(createFlight("AI101", "Air India", airports.get(0), airports.get(1), 
            "2025-12-01 08:00:00", "2025-12-01 10:30:00", 180, 5000.00));
        flights.add(createFlight("AI102", "Air India", airports.get(1), airports.get(0), 
            "2025-12-01 14:00:00", "2025-12-01 16:30:00", 180, 5000.00));
        flights.add(createFlight("6E201", "IndiGo", airports.get(0), airports.get(2), 
            "2025-12-01 06:00:00", "2025-12-01 08:45:00", 180, 4500.00));
        flights.add(createFlight("6E202", "IndiGo", airports.get(2), airports.get(0), 
            "2025-12-01 18:00:00", "2025-12-01 20:45:00", 180, 4500.00));
        flights.add(createFlight("SG301", "SpiceJet", airports.get(0), airports.get(3), 
            "2025-12-01 09:00:00", "2025-12-01 11:30:00", 150, 4200.00));
        flights.add(createFlight("UK401", "Vistara", airports.get(1), airports.get(2), 
            "2025-12-01 11:00:00", "2025-12-01 12:45:00", 150, 4800.00));
        
        // International flights
        flights.add(createFlight("AI501", "Air India", airports.get(0), airports.get(6), 
            "2025-12-02 22:00:00", "2025-12-03 10:30:00", 300, 55000.00));
        flights.add(createFlight("AI502", "Air India", airports.get(6), airports.get(0), 
            "2025-12-03 14:00:00", "2025-12-04 08:30:00", 300, 58000.00));
        
        // Save flights (seats will be auto-generated by the service layer if using API)
        List<Flight> savedFlights = flightRepository.saveAll(flights);
        
        // Generate seats for each flight
        for (Flight flight : savedFlights) {
            generateSeatsForFlight(flight);
        }
        
        return savedFlights;
    }
    
    private Flight createFlight(String flightNumber, String airlineName, Airport departure, 
                               Airport arrival, String departureTime, String arrivalTime, 
                               int totalSeats, double basePrice) {
        Flight flight = new Flight();
        flight.setFlightNumber(flightNumber);
        flight.setAirlineName(airlineName);
        flight.setDepartureAirport(departure);
        flight.setArrivalAirport(arrival);
        flight.setDepartureTime(LocalDateTime.parse(departureTime.replace(" ", "T")));
        flight.setArrivalTime(LocalDateTime.parse(arrivalTime.replace(" ", "T")));
        flight.setTotalSeats(totalSeats);
        flight.setAvailableSeats(totalSeats);
        flight.setBasePrice(basePrice);
        flight.setStatus(Flight.FlightStatus.SCHEDULED);
        return flight;
    }
    
    private void generateSeatsForFlight(Flight flight) {
        List<Seat> seats = new ArrayList<>();
        int totalSeats = flight.getTotalSeats();
        int seatsPerRow = 6; // A-F
        
        for (int row = 1; row <= (totalSeats / seatsPerRow); row++) {
            for (char seatLetter = 'A'; seatLetter < 'A' + seatsPerRow; seatLetter++) {
                Seat seat = new Seat();
                seat.setFlight(flight);
                seat.setSeatNumber(row + String.valueOf(seatLetter));
                
                // Assign seat class and price based on row
                if (row <= 2) {
                    seat.setSeatClass(Seat.SeatClass.FIRST_CLASS);
                    seat.setPrice(flight.getBasePrice() * 3);
                } else if (row <= 5) {
                    seat.setSeatClass(Seat.SeatClass.BUSINESS);
                    seat.setPrice(flight.getBasePrice() * 2);
                } else if (row <= 10) {
                    seat.setSeatClass(Seat.SeatClass.PREMIUM_ECONOMY);
                    seat.setPrice(flight.getBasePrice() * 1.5);
                } else {
                    seat.setSeatClass(Seat.SeatClass.ECONOMY);
                    seat.setPrice(flight.getBasePrice());
                }
                
                seat.setStatus(Seat.SeatStatus.AVAILABLE);
                seats.add(seat);
            }
        }
        
        seatRepository.saveAll(seats);
        log.info("Generated {} seats for flight {}", seats.size(), flight.getFlightNumber());
    }
}

