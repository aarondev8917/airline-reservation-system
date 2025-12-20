package com.airline.reservation.controllers;

import com.airline.reservation.dtos.BookingRequestDto;
import com.airline.reservation.dtos.BookingResponseDto;
import com.airline.reservation.exceptions.ResourceNotFoundException;
import com.airline.reservation.services.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = BookingController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.airline\\.reservation\\.security\\..*"
        )
)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("BookingController Unit Tests")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingRequestDto bookingRequest;
    private BookingResponseDto bookingResponse;

    @BeforeEach
    void setUp() {
        bookingRequest = new BookingRequestDto();
        bookingRequest.setPassengerId(1L);
        bookingRequest.setFlightId(1L);
        bookingRequest.setSeatId(1L);

        bookingResponse = new BookingResponseDto();
        bookingResponse.setId(1L);
        bookingResponse.setBookingReference("BK12345678");
        bookingResponse.setStatus("PENDING");
    }

    @Test
    @DisplayName("Should successfully create a booking")
    void testCreateBooking_Success() throws Exception {
        // Given
        when(bookingService.createBooking(any(BookingRequestDto.class))).thenReturn(bookingResponse);

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Booking created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.bookingReference").value("BK12345678"));

        verify(bookingService).createBooking(any(BookingRequestDto.class));
    }

    @Test
    @DisplayName("Should return 400 when booking creation fails validation")
    void testCreateBooking_ValidationError() throws Exception {
        // Given
        BookingRequestDto invalidRequest = new BookingRequestDto();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).createBooking(any());
    }

    @Test
    @DisplayName("Should successfully get booking by ID")
    void testGetBookingById_Success() throws Exception {
        // Given
        when(bookingService.getBookingById(1L)).thenReturn(bookingResponse);

        // When & Then
        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Booking retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(bookingService).getBookingById(1L);
    }

    @Test
    @DisplayName("Should return 404 when booking not found")
    void testGetBookingById_NotFound() throws Exception {
        // Given
        when(bookingService.getBookingById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Booking", 1L));

        // When & Then
        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isNotFound());

        verify(bookingService).getBookingById(1L);
    }

    @Test
    @DisplayName("Should successfully get booking by reference")
    void testGetBookingByReference_Success() throws Exception {
        // Given
        when(bookingService.getBookingByReference(anyString())).thenReturn(bookingResponse);

        // When & Then
        mockMvc.perform(get("/api/bookings/reference/BK12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookingReference").value("BK12345678"));

        verify(bookingService).getBookingByReference("BK12345678");
    }

    @Test
    @DisplayName("Should successfully get all bookings")
    void testGetAllBookings_Success() throws Exception {
        // Given
        BookingResponseDto response2 = new BookingResponseDto();
        response2.setId(2L);
        response2.setBookingReference("BK87654321");

        List<BookingResponseDto> bookings = Arrays.asList(bookingResponse, response2);
        when(bookingService.getAllBookings()).thenReturn(bookings);

        // When & Then
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(bookingService).getAllBookings();
    }

    @Test
    @DisplayName("Should successfully get bookings by passenger ID")
    void testGetBookingsByPassengerId_Success() throws Exception {
        // Given
        List<BookingResponseDto> bookings = Arrays.asList(bookingResponse);
        when(bookingService.getBookingsByPassengerId(1L)).thenReturn(bookings);

        // When & Then
        mockMvc.perform(get("/api/bookings/passenger/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(bookingService).getBookingsByPassengerId(1L);
    }

    @Test
    @DisplayName("Should successfully confirm a booking")
    void testConfirmBooking_Success() throws Exception {
        // Given
        bookingResponse.setStatus("CONFIRMED");
        when(bookingService.confirmBooking(1L)).thenReturn(bookingResponse);

        // When & Then
        mockMvc.perform(patch("/api/bookings/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Booking confirmed successfully"))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

        verify(bookingService).confirmBooking(1L);
    }

    @Test
    @DisplayName("Should successfully cancel a booking")
    void testCancelBooking_Success() throws Exception {
        // Given
        doNothing().when(bookingService).cancelBooking(1L);

        // When & Then
        mockMvc.perform(delete("/api/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Booking cancelled successfully"));

        verify(bookingService).cancelBooking(1L);
    }
}

