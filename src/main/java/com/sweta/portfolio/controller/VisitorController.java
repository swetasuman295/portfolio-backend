package com.sweta.portfolio.controller;

import com.sweta.portfolio.service.VisitorTrackingService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/visitor")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Visitor View", description = "APIs for managing portfolio visitor")
public class VisitorController {

    private final VisitorTrackingService visitorTrackingService;

    /**
     * Track when someone visits the portfolio
     * Called automatically when Angular app loads
     */
    @PostMapping("/session")
    public ResponseEntity<Map<String, String>> trackSession(HttpServletRequest request,HttpServletResponse response) {
        log.info("=== Received session tracking request ===");
        try {
            visitorTrackingService.trackVisitorSession(request,response);
            
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("status", "SUCCESS");
            String sessionId = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("PORTFOLIO_SESSION_ID".equals(cookie.getName())) {
                        sessionId = cookie.getValue();
                        break;
                    }
                }
            }
            responseMap.put("sessionId", sessionId != null ? sessionId : "unknown");

            responseMap.put("message", "Session tracked successfully");
            
            log.info("Session tracked successfully: {}", request.getSession().getId());
            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            log.error("Failed to track visitor session", e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Failed to track session: " + e.getMessage());
            
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Track page views within the portfolio
     * Called when user navigates between sections
     */
    @PostMapping("/pageview")
    public ResponseEntity<Map<String, String>> trackPageView(
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        log.info("=== Received page view tracking request: {} ===", payload);
        try {
            String sessionId = request.getSession().getId();
            String page = (String) payload.get("page");
            String previousPage = (String) payload.get("previousPage");
            Long timeSpent = payload.get("timeSpent") != null ?
                    Long.valueOf(payload.get("timeSpent").toString()) : 0L;

            visitorTrackingService.trackPageView(sessionId, page, previousPage, timeSpent);

            Map<String, String> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Page view tracked");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to track page view", e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Failed to track page view: " + e.getMessage());
            
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Get current session info (for debugging)
     */
    @GetMapping("/session/info")
    public ResponseEntity<Map<String, String>> getSessionInfo(HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        Map<String, String> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("ipAddress", ipAddress);
        response.put("userAgent", userAgent != null ? userAgent : "Unknown");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Simple test endpoint
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        log.info("=== Test endpoint called ===");
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "VisitorController is working");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "VisitorTrackingService");
        
        return ResponseEntity.ok(response);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }

    @PostConstruct
    public void init() {
        log.info("====== VisitorController created successfully ======");
        log.info("Available endpoints:");
        log.info("POST /api/visitor/session - Track visitor sessions");
        log.info("POST /api/visitor/pageview - Track page views");
        log.info("GET /api/visitor/session/info - Get session info");
        log.info("GET /api/visitor/test - Test endpoint");
        log.info("GET /api/visitor/health - Health check");
    }
}