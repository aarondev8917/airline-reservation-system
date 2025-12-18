package com.airline.reservation.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration class for JPA and Transaction Management
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.airline.reservation.repositories")
@EnableTransactionManagement
public class JpaConfig {
    // Additional JPA configurations can be added here if needed
}

