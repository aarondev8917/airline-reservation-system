package com.airline.reservation.security;

import com.airline.reservation.models.AppUser;
import com.airline.reservation.models.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AppUserPrincipal Unit Tests")
class AppUserPrincipalTest {

    private AppUser appUser;
    private AppUserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        appUser = new AppUser();
        appUser.setId(1L);
        appUser.setEmail("test@example.com");
        appUser.setPassword("encodedPassword");
        appUser.setRole(Role.USER);
        appUser.setEnabled(true);

        userPrincipal = new AppUserPrincipal(appUser);
    }

    @Test
    @DisplayName("Should return correct authorities for USER role")
    void testGetAuthorities_UserRole() {
        // When
        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();

        // Then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("Should return correct authorities for ADMIN role")
    void testGetAuthorities_AdminRole() {
        // Given
        appUser.setRole(Role.ADMIN);
        AppUserPrincipal adminPrincipal = new AppUserPrincipal(appUser);

        // When
        Collection<? extends GrantedAuthority> authorities = adminPrincipal.getAuthorities();

        // Then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Should return correct password")
    void testGetPassword() {
        // When
        String password = userPrincipal.getPassword();

        // Then
        assertEquals("encodedPassword", password);
    }

    @Test
    @DisplayName("Should return email as username")
    void testGetUsername() {
        // When
        String username = userPrincipal.getUsername();

        // Then
        assertEquals("test@example.com", username);
    }

    @Test
    @DisplayName("Should return true for account non-expired")
    void testIsAccountNonExpired() {
        // When
        boolean isNonExpired = userPrincipal.isAccountNonExpired();

        // Then
        assertTrue(isNonExpired);
    }

    @Test
    @DisplayName("Should return true for account non-locked")
    void testIsAccountNonLocked() {
        // When
        boolean isNonLocked = userPrincipal.isAccountNonLocked();

        // Then
        assertTrue(isNonLocked);
    }

    @Test
    @DisplayName("Should return true for credentials non-expired")
    void testIsCredentialsNonExpired() {
        // When
        boolean isNonExpired = userPrincipal.isCredentialsNonExpired();

        // Then
        assertTrue(isNonExpired);
    }

    @Test
    @DisplayName("Should return enabled status from user")
    void testIsEnabled() {
        // Given
        appUser.setEnabled(true);
        AppUserPrincipal enabledPrincipal = new AppUserPrincipal(appUser);

        // When
        boolean isEnabled = enabledPrincipal.isEnabled();

        // Then
        assertTrue(isEnabled);

        // Given
        appUser.setEnabled(false);
        AppUserPrincipal disabledPrincipal = new AppUserPrincipal(appUser);

        // When
        boolean isDisabled = disabledPrincipal.isEnabled();

        // Then
        assertFalse(isDisabled);
    }

    @Test
    @DisplayName("Should return the underlying AppUser")
    void testGetUser() {
        // When
        AppUser retrievedUser = userPrincipal.getUser();

        // Then
        assertNotNull(retrievedUser);
        assertEquals(appUser, retrievedUser);
        assertEquals("test@example.com", retrievedUser.getEmail());
    }
}

