package com.airline.reservation.services;

import com.airline.reservation.dtos.AuthResponseDto;
import com.airline.reservation.dtos.LoginRequestDto;
import com.airline.reservation.dtos.RegisterRequestDto;
import com.airline.reservation.dtos.UserInfoDto;
import com.airline.reservation.exceptions.ResourceAlreadyExistsException;
import com.airline.reservation.models.AppUser;
import com.airline.reservation.models.Role;
import com.airline.reservation.repositories.AppUserRepository;
import com.airline.reservation.security.AppUserPrincipal;
import com.airline.reservation.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponseDto register(RegisterRequestDto request) {
        final String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("User already exists with email: " + email);
        }

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(true);

        AppUser saved = userRepository.save(user);
        String token = jwtService.generateToken(new AppUserPrincipal(saved));

        return new AuthResponseDto(
                token,
                "Bearer",
                jwtService.getJwtExpirationMs(),
                new UserInfoDto(saved.getEmail(), saved.getRole().name())
        );
    }

    public AuthResponseDto login(LoginRequestDto request) {
        final String email = request.getEmail().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));

        String token = jwtService.generateToken(new AppUserPrincipal(user));

        return new AuthResponseDto(
                token,
                "Bearer",
                jwtService.getJwtExpirationMs(),
                new UserInfoDto(user.getEmail(), user.getRole().name())
        );
    }
}


