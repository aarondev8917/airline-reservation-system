package com.airline.reservation.configs;

import com.airline.reservation.models.AppUser;
import com.airline.reservation.models.Role;
import com.airline.reservation.repositories.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds default auth users for local/dev testing.
 */
@Configuration
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class AuthDataInitializer {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initAuthUsers() {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            AppUser admin = new AppUser();
            admin.setEmail("admin@airline.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);

            AppUser user = new AppUser();
            user.setEmail("user@airline.com");
            user.setPassword(passwordEncoder.encode("user1234"));
            user.setRole(Role.USER);
            user.setEnabled(true);
            userRepository.save(user);
        };
    }
}


