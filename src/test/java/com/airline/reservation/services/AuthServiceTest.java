package com.airline.reservation.services;

import com.airline.reservation.dtos.AuthResponseDto;
import com.airline.reservation.dtos.LoginRequestDto;
import com.airline.reservation.dtos.RegisterRequestDto;
import com.airline.reservation.exceptions.ResourceAlreadyExistsException;
import com.airline.reservation.models.AppUser;
import com.airline.reservation.models.Role;
import com.airline.reservation.repositories.AppUserRepository;
import com.airline.reservation.security.AppUserPrincipal;
import com.airline.reservation.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDto registerRequest;
    private LoginRequestDto loginRequest;
    private AppUser testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequestDto();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        testUser = new AppUser();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);
        testUser.setEnabled(true);
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegister_Success() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(AppUser.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(AppUserPrincipal.class))).thenReturn("jwt-token");
        when(jwtService.getJwtExpirationMs()).thenReturn(86400000L);

        // When
        AuthResponseDto response = authService.register(registerRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("test@example.com", response.getUser().getEmail());
        assertEquals("USER", response.getUser().getRole());

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(AppUser.class));
        verify(jwtService).generateToken(any(AppUserPrincipal.class));
    }

    @Test
    @DisplayName("Should throw exception when registering with existing email")
    void testRegister_EmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            authService.register(registerRequest);
        });

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    @DisplayName("Should normalize email to lowercase when registering")
    void testRegister_EmailNormalization() {
        // Given
        registerRequest.setEmail("TEST@EXAMPLE.COM");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(AppUser.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(AppUserPrincipal.class))).thenReturn("jwt-token");
        when(jwtService.getJwtExpirationMs()).thenReturn(86400000L);

        // When
        AuthResponseDto response = authService.register(registerRequest);

        // Then
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(argThat(user -> 
            "test@example.com".equals(user.getEmail())
        ));
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLogin_Success() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(AppUserPrincipal.class))).thenReturn("jwt-token");
        when(jwtService.getJwtExpirationMs()).thenReturn(86400000L);

        // When
        AuthResponseDto response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("test@example.com", response.getUser().getEmail());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtService).generateToken(any(AppUserPrincipal.class));
    }

    @Test
    @DisplayName("Should throw exception when login credentials are invalid")
    void testLogin_InvalidCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should throw exception when authenticated user not found")
    void testLogin_UserNotFoundAfterAuthentication() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            authService.login(loginRequest);
        });

        verify(userRepository).findByEmail("test@example.com");
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should normalize email to lowercase when logging in")
    void testLogin_EmailNormalization() {
        // Given
        loginRequest.setEmail("TEST@EXAMPLE.COM");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(AppUserPrincipal.class))).thenReturn("jwt-token");
        when(jwtService.getJwtExpirationMs()).thenReturn(86400000L);

        // When
        authService.login(loginRequest);

        // Then
        verify(userRepository).findByEmail("test@example.com");
    }
}

