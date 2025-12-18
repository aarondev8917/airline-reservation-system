-- Sample Data for Airline Reservation System
-- This file contains sample data to test the application

-- Insert Airports
INSERT INTO airports (code, name, city, country) VALUES
('DEL', 'Indira Gandhi International Airport', 'New Delhi', 'India'),
('BOM', 'Chhatrapati Shivaji Maharaj International Airport', 'Mumbai', 'India'),
('BLR', 'Kempegowda International Airport', 'Bangalore', 'India'),
('MAA', 'Chennai International Airport', 'Chennai', 'India'),
('HYD', 'Rajiv Gandhi International Airport', 'Hyderabad', 'India'),
('CCU', 'Netaji Subhas Chandra Bose International Airport', 'Kolkata', 'India'),
('JFK', 'John F. Kennedy International Airport', 'New York', 'USA'),
('LAX', 'Los Angeles International Airport', 'Los Angeles', 'USA'),
('LHR', 'London Heathrow Airport', 'London', 'UK'),
('DXB', 'Dubai International Airport', 'Dubai', 'UAE');

-- Insert Sample Passengers
INSERT INTO passengers (first_name, last_name, email, phone_number, date_of_birth, passport_number, nationality) VALUES
('Rajesh', 'Kumar', 'rajesh.kumar@email.com', '9876543210', '1990-05-15', 'A1234567', 'India'),
('Priya', 'Sharma', 'priya.sharma@email.com', '9876543211', '1992-08-22', 'A1234568', 'India'),
('Amit', 'Patel', 'amit.patel@email.com', '9876543212', '1988-03-10', 'A1234569', 'India'),
('Sneha', 'Reddy', 'sneha.reddy@email.com', '9876543213', '1995-11-30', 'A1234570', 'India'),
('Vikram', 'Singh', 'vikram.singh@email.com', '9876543214', '1987-07-18', 'A1234571', 'India');

-- Insert Sample Flights
INSERT INTO flights (flight_number, airline_name, departure_airport_id, arrival_airport_id, departure_time, arrival_time, total_seats, available_seats, base_price, status) VALUES
('AI101', 'Air India', 1, 2, '2025-12-01 08:00:00', '2025-12-01 10:30:00', 180, 180, 5000.00, 'SCHEDULED'),
('AI102', 'Air India', 2, 1, '2025-12-01 14:00:00', '2025-12-01 16:30:00', 180, 180, 5000.00, 'SCHEDULED'),
('6E201', 'IndiGo', 1, 3, '2025-12-01 06:00:00', '2025-12-01 08:45:00', 180, 180, 4500.00, 'SCHEDULED'),
('6E202', 'IndiGo', 3, 1, '2025-12-01 18:00:00', '2025-12-01 20:45:00', 180, 180, 4500.00, 'SCHEDULED'),
('SG301', 'SpiceJet', 1, 4, '2025-12-01 09:00:00', '2025-12-01 11:30:00', 150, 150, 4200.00, 'SCHEDULED'),
('SG302', 'SpiceJet', 4, 1, '2025-12-01 16:00:00', '2025-12-01 18:30:00', 150, 150, 4200.00, 'SCHEDULED'),
('UK401', 'Vistara', 2, 3, '2025-12-01 11:00:00', '2025-12-01 12:45:00', 150, 150, 4800.00, 'SCHEDULED'),
('UK402', 'Vistara', 3, 2, '2025-12-01 15:00:00', '2025-12-01 16:45:00', 150, 150, 4800.00, 'SCHEDULED'),
('AI501', 'Air India', 1, 7, '2025-12-02 22:00:00', '2025-12-03 10:30:00', 300, 300, 55000.00, 'SCHEDULED'),
('AI502', 'Air India', 7, 1, '2025-12-03 14:00:00', '2025-12-04 08:30:00', 300, 300, 58000.00, 'SCHEDULED');

-- Note: Seats will be automatically generated when flights are created through the application
-- The application creates seats with different classes (Economy, Premium Economy, Business, First Class)
-- This is handled by the FlightService.createSeatsForFlight() method

-- Sample note for testing:
-- After the application starts, you can use the API to:
-- 1. Search for flights
-- 2. View available seats
-- 3. Create bookings
-- 4. Process payments

