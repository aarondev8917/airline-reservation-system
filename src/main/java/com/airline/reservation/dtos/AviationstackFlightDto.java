package com.airline.reservation.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * DTO representing a flight from Aviationstack API.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AviationstackFlightDto {
    private AviationstackFlightInfoDto flight;
    private AviationstackAirlineDto airline;
    private AviationstackAirportInfoDto departure;
    private AviationstackAirportInfoDto arrival;
    private String flightDate;
    private String flightStatus;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class AviationstackFlightInfoDto {
    private String number;
    private String iata;
    private String icao;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class AviationstackAirlineDto {
    private String name;
    private String iata;
    private String icao;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class AviationstackAirportInfoDto {
    private AviationstackAirportDto airport;
    private String timezone;
    private String iata;
    private String icao;
    private String terminal;
    private String gate;
    private String delay;
    private String scheduled;
    private String estimated;
    private String actual;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class AviationstackAirportDto {
    private String airportName;
    private String iata;
    private String icao;
    private String city;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class AviationstackPaginationDto {
    private Integer limit;
    private Integer offset;
    private Integer count;
    private Integer total;
}
