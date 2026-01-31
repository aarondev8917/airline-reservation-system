package com.airline.reservation.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Aviationstack API error payload (returned in HTTP 200 body on auth/validation failures).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AviationstackErrorDto {
    private String code;
    private String message;
}
