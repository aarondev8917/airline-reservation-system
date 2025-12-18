package com.airline.reservation.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDto {
    private String token;
    private String tokenType;
    private long expiresInMs;
    private UserInfoDto user;
}


