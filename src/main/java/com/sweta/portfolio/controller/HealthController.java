package com.sweta.portfolio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "Portfolio Backend");
        response.put("version", "1.0.0");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "Portfolio Backend");
        response.put("version", "1.0.0");

        // System info
        Map<String, Object> system = new HashMap<>();
        system.put("java_version", System.getProperty("java.version"));
        system.put("spring_profiles", System.getProperty("spring.profiles.active", "default"));
        system.put("available_processors", Runtime.getRuntime().availableProcessors());
        system.put("max_memory", Runtime.getRuntime().maxMemory());
        system.put("free_memory", Runtime.getRuntime().freeMemory());

        response.put("system", system);

        // Services status (placeholder - you'll expand this later)
        Map<String, String> services = new HashMap<>();
        services.put("database", "UP");
        services.put("kafka", "UP");
        services.put("websocket", "UP");
        services.put("ai_service", "UP");

        response.put("services", services);

        return ResponseEntity.ok(response);
    }
}