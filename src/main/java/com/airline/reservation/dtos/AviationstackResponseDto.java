package com.airline.reservation.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * DTO representing Aviationstack API response wrapper.
 * API may return HTTP 200 with {@code error} object for auth/validation failures.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AviationstackResponseDto {
    private List<AviationstackFlightDto> data;
    private AviationstackPaginationDto pagination;
    private AviationstackErrorDto error;
}
