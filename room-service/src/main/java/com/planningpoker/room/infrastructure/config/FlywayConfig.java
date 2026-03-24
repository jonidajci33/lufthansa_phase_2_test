package com.planningpoker.room.infrastructure.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

/**
 * Explicit Flyway configuration for database migrations.
 * Spring Boot 4.x auto-configuration does not trigger Flyway reliably,
 * so we configure it manually. Runs BEFORE JPA/Hibernate initialization.
 */
@Configuration
public class FlywayConfig {

    @Bean
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas("room")
                .locations("classpath:db/migration")
                .createSchemas(true)
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load();
        flyway.repair();
        flyway.migrate();
        return flyway;
    }
}
