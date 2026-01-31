package com.airline.reservation.services;

import com.airline.reservation.dtos.AviationstackMapper;
import com.airline.reservation.dtos.AviationstackResponseDto;
import com.airline.reservation.dtos.ExternalFlightDto;
import com.airline.reservation.configs.CacheConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for integrating with Aviationstack API for real-time flight data.
 *
 * Configuration:
 * - external.flights.api-key: Aviationstack API access key (required for real API calls)
 * - external.flights.use-mock: Set to 'true' to use mock data instead of calling Aviationstack API
 *
 * If the API call fails or use-mock is enabled, the service falls back to mock data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalFlightApiService {

    private final RestTemplate restTemplate;

    private static final String AVIATIONSTACK_BASE_URL = "https://api.aviationstack.com/v1/flights";

    @Value("${external.flights.api-key:}")
    private String apiKey;

    @Value("${external.flights.use-mock:true}")
    private boolean useMock;

    /**
     * Fetch all flights from Aviationstack API or return mock data.
     */
    @Cacheable(cacheNames = CacheConfig.CACHE_EXTERNAL_FLIGHTS, key = "'all'")
    public List<ExternalFlightDto> fetchAllExternalFlights() {
        if (useMock || apiKey == null || apiKey.isEmpty()) {
            log.info("Using mock flight data (use-mock=true or no API key configured)");
            return getMockFlights();
        }

        try {
            String url = buildAviationstackUrl(null, null, null, 10);
            log.info("Calling Aviationstack API (flights, limit=10)");

            ResponseEntity<AviationstackResponseDto> response =
                    restTemplate.getForEntity(url, AviationstackResponseDto.class);

            AviationstackResponseDto body = response.getBody();
            if (body == null) {
                log.warn("Aviationstack API returned null body, falling back to mock data");
                return getMockFlights();
            }
            if (body.getError() != null) {
                log.warn("Aviationstack API error: code={}, message={}. Falling back to mock data",
                        body.getError().getCode(), body.getError().getMessage());
                return getMockFlights();
            }
            if (body.getData() == null || body.getData().isEmpty()) {
                log.warn("Aviationstack API returned empty data, falling back to mock data");
                return getMockFlights();
            }

            List<ExternalFlightDto> flights = body.getData().stream()
                    .map(AviationstackMapper::toExternalFlightDto)
                    .collect(Collectors.toList());

            log.info("Fetched {} flights from Aviationstack API", flights.size());
            return flights;
        } catch (RestClientException e) {
            log.error("Aviationstack API request failed: {}. Falling back to mock data", e.getMessage());
            return getMockFlights();
        }
    }

    /**
     * Fetch flights by flight number from Aviationstack API or return mock data.
     */
    @Cacheable(cacheNames = CacheConfig.CACHE_EXTERNAL_FLIGHTS, key = "'number:' + #flightNumber")
    public List<ExternalFlightDto> fetchFlightsByFlightNumber(String flightNumber) {
        if (useMock || apiKey == null || apiKey.isEmpty()) {
            log.info("Using mock flight data for flight number: {}", flightNumber);
            return getMockFlights().stream()
                    .filter(f -> f.getFlightNumber().equalsIgnoreCase(flightNumber))
                    .collect(Collectors.toList());
        }

        try {
            String url = buildAviationstackUrl(flightNumber, null, null, 10);
            log.info("Calling Aviationstack API for flight {}", flightNumber);

            ResponseEntity<AviationstackResponseDto> response =
                    restTemplate.getForEntity(url, AviationstackResponseDto.class);

            AviationstackResponseDto body = response.getBody();
            if (body == null) {
                log.warn("Aviationstack API returned null body for flight {}", flightNumber);
                return Collections.emptyList();
            }
            if (body.getError() != null) {
                log.warn("Aviationstack API error for flight {}: code={}, message={}",
                        flightNumber, body.getError().getCode(), body.getError().getMessage());
                return Collections.emptyList();
            }
            if (body.getData() == null || body.getData().isEmpty()) {
                log.debug("Aviationstack API returned no data for flight {}", flightNumber);
                return Collections.emptyList();
            }

            List<ExternalFlightDto> flights = body.getData().stream()
                    .map(AviationstackMapper::toExternalFlightDto)
                    .collect(Collectors.toList());

            log.info("Fetched {} flights from Aviationstack API for flight {}", flights.size(), flightNumber);
            return flights;
        } catch (RestClientException e) {
            log.error("Aviationstack API request failed for flight {}: {}", flightNumber, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Fetch flights by route (departure and arrival IATA codes) from Aviationstack API.
     * Used for unified search with internal flights.
     */
    @Cacheable(cacheNames = CacheConfig.CACHE_EXTERNAL_FLIGHTS, key = "'route:' + #depIata + '-' + #arrIata")
    public List<ExternalFlightDto> fetchFlightsByRoute(String depIata, String arrIata) {
        if (useMock || apiKey == null || apiKey.isEmpty()) {
            log.info("Using mock flight data for route {} -> {}", depIata, arrIata);
            return getMockFlights().stream()
                    .filter(f -> matchesRoute(f, depIata, arrIata))
                    .collect(Collectors.toList());
        }

        try {
            String url = buildAviationstackUrl(null, depIata, arrIata, 10);
            log.info("Calling Aviationstack API for route {} -> {}", depIata, arrIata);

            ResponseEntity<AviationstackResponseDto> response =
                    restTemplate.getForEntity(url, AviationstackResponseDto.class);

            AviationstackResponseDto body = response.getBody();
            if (body == null) {
                log.warn("Aviationstack API returned null body for route {} -> {}", depIata, arrIata);
                return Collections.emptyList();
            }
            if (body.getError() != null) {
                log.warn("Aviationstack API error for route: code={}, message={}",
                        body.getError().getCode(), body.getError().getMessage());
                return Collections.emptyList();
            }
            if (body.getData() == null || body.getData().isEmpty()) {
                log.debug("Aviationstack API returned no data for route {} -> {}", depIata, arrIata);
                return Collections.emptyList();
            }

            List<ExternalFlightDto> flights = body.getData().stream()
                    .map(AviationstackMapper::toExternalFlightDto)
                    .collect(Collectors.toList());

            log.info("Fetched {} flights from Aviationstack API for route {} -> {}", flights.size(), depIata, arrIata);
            return flights;
        } catch (RestClientException e) {
            log.error("Aviationstack API request failed for route {} -> {}: {}", depIata, arrIata, e.getMessage());
            return Collections.emptyList();
        }
    }

    private static boolean matchesRoute(ExternalFlightDto f, String dep, String arr) {
        if (dep == null || arr == null) return true;
        return (f.getOrigin() != null && f.getOrigin().equalsIgnoreCase(dep))
                && (f.getDestination() != null && f.getDestination().equalsIgnoreCase(arr));
    }

    /**
     * Fetch a single flight from Aviationstack API by flight number or return mock data.
     * Note: Aviationstack doesn't have a direct "by ID" endpoint, so we use flight number.
     */
    @Cacheable(cacheNames = CacheConfig.CACHE_EXTERNAL_FLIGHTS, key = "'id:' + #id")
    public ExternalFlightDto fetchExternalFlightById(String id) {
        List<ExternalFlightDto> flights = fetchFlightsByFlightNumber(id);
        if (!flights.isEmpty()) {
            return flights.get(0);
        }
        return getMockFlightById(id);
    }

    /**
     * Returns mock flight data for testing/demo purposes.
     */
    private List<ExternalFlightDto> getMockFlights() {
        List<ExternalFlightDto> mockFlights = new ArrayList<>();
        LocalDateTime base = LocalDate.now().atTime(10, 0);
        mockFlights.add(createMockFlight("1", "AA101", "American Airlines", "JFK", "LAX", 299.99, base, base.plusHours(5)));
        mockFlights.add(createMockFlight("2", "DL205", "Delta Airlines", "ATL", "SFO", 349.99, base.plusHours(2), base.plusHours(7)));
        mockFlights.add(createMockFlight("3", "UA310", "United Airlines", "ORD", "MIA", 249.99, base.plusHours(1), base.plusHours(3)));
        mockFlights.add(createMockFlight("4", "SW450", "Southwest Airlines", "DEN", "SEA", 199.99, base.plusHours(3), base.plusHours(5)));
        mockFlights.add(createMockFlight("5", "BA501", "British Airways", "LHR", "JFK", 599.99, base.plusHours(4), base.plusHours(10)));
        mockFlights.add(createMockFlight("6", "LH601", "Lufthansa", "FRA", "DXB", 449.99, base.plusHours(5), base.plusHours(12)));
        mockFlights.add(createMockFlight("7", "EK701", "Emirates", "DXB", "SIN", 399.99, base.plusHours(6), base.plusHours(14)));
        mockFlights.add(createMockFlight("8", "QF801", "Qantas", "SYD", "LAX", 699.99, base.plusHours(7), base.plusHours(22)));

        return mockFlights;
    }

    /**
     * Returns a single mock flight by ID, or the first mock flight if ID not found.
     */
    private ExternalFlightDto getMockFlightById(String id) {
        List<ExternalFlightDto> mockFlights = getMockFlights();
        return mockFlights.stream()
                .filter(flight -> flight.getId().equals(id))
                .findFirst()
                .orElse(mockFlights.get(0)); // Return first flight if ID not found
    }

    private ExternalFlightDto createMockFlight(String id, String flightNumber, String airline,
                                               String origin, String destination, Double price,
                                               LocalDateTime dep, LocalDateTime arr) {
        ExternalFlightDto flight = new ExternalFlightDto();
        flight.setId(id);
        flight.setFlightNumber(flightNumber);
        flight.setAirline(airline);
        flight.setOrigin(origin);
        flight.setDestination(destination);
        flight.setPrice(price);
        flight.setDepartureTime(dep);
        flight.setArrivalTime(arr);
        return flight;
    }

    /** Builds Aviationstack API URL. Never log the returned string (contains API key). */
    private String buildAviationstackUrl(String flightIata, String depIata, String arrIata, int limit) {
        var builder = UriComponentsBuilder.fromHttpUrl(AVIATIONSTACK_BASE_URL)
                .queryParam("access_key", apiKey)
                .queryParam("limit", limit);
        if (flightIata != null && !flightIata.isBlank()) {
            builder.queryParam("flight_iata", flightIata);
        }
        if (depIata != null && !depIata.isBlank()) {
            builder.queryParam("dep_iata", depIata);
        }
        if (arrIata != null && !arrIata.isBlank()) {
            builder.queryParam("arr_iata", arrIata);
        }
        return builder.toUriString();
    }
}

