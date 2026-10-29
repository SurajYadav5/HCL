package com.portfoliopro.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class PublicHealthController {

    @GetMapping({"/", "/health", "/status", "/ping"})
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP",
            "app", "PortfolioPro Institutional Platform",
            "version", "1.0.0",
            "timestamp", Instant.now().toString()
        );
    }
}
