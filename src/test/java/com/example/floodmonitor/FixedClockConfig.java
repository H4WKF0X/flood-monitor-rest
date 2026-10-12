package com.example.floodmonitor;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@TestConfiguration
public class FixedClockConfig {
    public static final Instant NOW = Instant.parse("2026-05-01T12:00:00Z");

    @Bean
    @Primary
    public Clock fixedTestClock() {
        return Clock.fixed(NOW, ZoneOffset.UTC);
    }
}
