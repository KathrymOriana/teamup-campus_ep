package com.campus.teamup.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AppConfig {

    /** Reloj inyectable: las reglas con fechas (vencidas, avance semanal) se pueden probar con una fecha fija. */
    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
