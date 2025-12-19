package com.airline.reservation.security;

import com.airline.reservation.models.AppUser;
import com.airline.reservation.models.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private AppUser testUser;
    private AppUserPrincipal userPrincipal;
    private String testSecret;

    @BeforeEach
    void setUp() {
        // Generate a valid Base64-encoded secret key for testing
        Key key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        testSecret = Base64.getEncoder().encodeToString(key.getEncoded());

        // Set the secret using reflection
        ReflectionTestUtils.setField(jwtService, "jwtSecretBase64", testSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 86400000L); // 24 hours

        testUser = new AppUser();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);
        testUser.setEnabled(true);

        userPrincipal = new AppUserPrincipal(testUser);
    }

    @Test
    @DisplayName("Should generate a valid JWT token")
    void testGenerateToken_Success() {
        // When
        String token = jwtService.generateToken(userPrincipal);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    @DisplayName("Should extract username from token")
    void testExtractUsername_Success() {
        // Given
        String token = jwtService.generateToken(userPrincipal);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertEquals("test@example.com", username);
    }

    @Test
    @DisplayName("Should validate token successfully with valid token")
    void testIsTokenValid_ValidToken() {
        // Given
        String token = jwtService.generateToken(userPrincipal);

        // When
        boolean isValid = jwtService.isTokenValid(token, userPrincipal);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should return false for token with different username")
    void testIsTokenValid_DifferentUsername() {
        // Given
        String token = jwtService.generateToken(userPrincipal);

        AppUser differentUser = new AppUser();
        differentUser.setEmail("different@example.com");
        AppUserPrincipal differentPrincipal = new AppUserPrincipal(differentUser);

        // When
        boolean isValid = jwtService.isTokenValid(token, differentPrincipal);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should extract expiration from token")
    void testExtractExpiration_Success() {
        // Given
        String token = jwtService.generateToken(userPrincipal);

        // When
        Date expiration = jwtService.extractClaim(token, claims -> claims.getExpiration());

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("Should extract role claim from token")
    void testExtractRoleClaim_Success() {
        // Given
        String token = jwtService.generateToken(userPrincipal);

        // When
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

        // Then
        assertNotNull(role);
        assertEquals("USER", role);
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void testGenerateToken_DifferentUsers() {
        // Given
        AppUser user2 = new AppUser();
        user2.setEmail("user2@example.com");
        user2.setRole(Role.ADMIN);
        AppUserPrincipal principal2 = new AppUserPrincipal(user2);

        // When
        String token1 = jwtService.generateToken(userPrincipal);
        String token2 = jwtService.generateToken(principal2);

        // Then
        assertNotEquals(token1, token2);
        
        // Verify they contain different usernames
        String username1 = jwtService.extractUsername(token1);
        String username2 = jwtService.extractUsername(token2);
        assertNotEquals(username1, username2);
    }

    @Test
    @DisplayName("Should return correct expiration time")
    void testGetJwtExpirationMs() {
        // When
        long expiration = jwtService.getJwtExpirationMs();

        // Then
        assertEquals(86400000L, expiration); // 24 hours in milliseconds
    }

    @Test
    @DisplayName("Should include role in token claims for AppUserPrincipal")
    void testGenerateToken_IncludesRoleClaim() {
        // When
        String token = jwtService.generateToken(userPrincipal);

        // Then
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        assertEquals("USER", role);
    }

    @Test
    @DisplayName("Should handle ADMIN role in token")
    void testGenerateToken_AdminRole() {
        // Given
        testUser.setRole(Role.ADMIN);
        AppUserPrincipal adminPrincipal = new AppUserPrincipal(testUser);

        // When
        String token = jwtService.generateToken(adminPrincipal);

        // Then
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        assertEquals("ADMIN", role);
    }

    @Test
    @DisplayName("Should extract custom claims from token")
    void testExtractClaim_CustomClaim() {
        // Given
        String token = jwtService.generateToken(userPrincipal);

        // When
        Date issuedAt = jwtService.extractClaim(token, claims -> claims.getIssuedAt());

        // Then
        assertNotNull(issuedAt);
        assertTrue(issuedAt.before(new Date()) || issuedAt.equals(new Date()));
    }
}

