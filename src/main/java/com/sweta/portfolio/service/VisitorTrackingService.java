package com.sweta.portfolio.service;

import com.sweta.portfolio.kafka.ContactEventProducer;
import com.sweta.portfolio.kafka.events.VisitorSessionEvent;
import com.sweta.portfolio.kafka.events.PageViewEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitorTrackingService {

    private final ContactEventProducer eventProducer;
    @Value("${kafka.topics.visitor-events}")
    private String visitorEventsTopic;
    private static final String SESSION_COOKIE_NAME = "PORTFOLIO_SESSION_ID";
    private static final int SESSION_COOKIE_MAX_AGE = 3600; // 1 hour
    
    /**
     * Track a visitor session (when someone first visits the portfolio)
     */
    public void trackVisitorSession(HttpServletRequest request ,HttpServletResponse response) {
        try {
        	String sessionId = getOrCreateSessionId(request, response);
            String ipAddress = getClientIP(request);
            String userAgent = request.getHeader("User-Agent");
            String location = getLocationFromIP(ipAddress);
            String page = getPageFromRequest(request);
            String referrer = request.getHeader("Referer");

            VisitorSessionEvent event = VisitorSessionEvent.builder()
                    .sessionId(sessionId)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .location(location)
                    .page(page)
                    .referrer(referrer)
                    .deviceType(detectDeviceType(userAgent))
                    .eventType("VISITOR_SESSION")
                    .build();
            log.info(">>>>>> PRODUCER: Attempting to publish event to topic 'visitor-events'");
            
            eventProducer.publishEvent(visitorEventsTopic, sessionId, event);
            
            log.info("Visitor session tracked: {} from {}", sessionId, location);
            
        } catch (Exception e) {
            log.error("Failed to track visitor session", e);
        }
    }
    private String getOrCreateSessionId(HttpServletRequest request, HttpServletResponse response) {
        // Check for existing session cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SESSION_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        // Create new session ID
        String newSessionId = UUID.randomUUID().toString();
        Cookie sessionCookie = new Cookie(SESSION_COOKIE_NAME, newSessionId);
        sessionCookie.setMaxAge(SESSION_COOKIE_MAX_AGE);
        sessionCookie.setPath("/");
        sessionCookie.setHttpOnly(true);
        sessionCookie.setSecure(true);
        response.addCookie(sessionCookie);
        
        return newSessionId;
    }

    /**
     * Track page views within a session
     */
    public void trackPageView(String sessionId, String page, String previousPage, Long timeSpentSeconds) {
        try {
            PageViewEvent event = PageViewEvent.builder()
                    .sessionId(sessionId)
                    .page(page)
                    .previousPage(previousPage)
                    .timeSpentSeconds(timeSpentSeconds)
                    .eventType("PAGE_VIEW")
                    .build();

            // Use your existing event producer
            eventProducer.publishEvent(visitorEventsTopic, sessionId, event);
            
            log.info("Page view tracked: {} -> {} ({}s)", previousPage, page, timeSpentSeconds);
            
        } catch (Exception e) {
            log.error("Failed to track page view", e);
        }
    }

    /**
     * Extract client IP address from request
     */
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

    /**
     * Get location from IP address (simplified for demo)
     */
    private String getLocationFromIP(String ipAddress) {
        // For demo purposes, we'll simulate location detection
        // In production, you'd use a service like MaxMind GeoIP2
        
        if (ipAddress == null || ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
            return "Local Development";
        }
        
        // Simulate different Dutch locations for demo
        String[] locations = {
            "Amsterdam, Netherlands",
            "Rotterdam, Netherlands", 
            "Utrecht, Netherlands",
            "Eindhoven, Netherlands",
            "Den Haag, Netherlands"
        };
        
        // Use IP hash to consistently assign same location to same IP
        int locationIndex = Math.abs(ipAddress.hashCode()) % locations.length;
        return locations[locationIndex];
    }

    /**
     * Detect device type from user agent
     */
    private String detectDeviceType(String userAgent) {
        if (userAgent == null) return "Unknown";
        
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "Mobile";
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return "Tablet";
        }
        return "Desktop";
    }

    /**
     * Extract page name from request
     */
    private String getPageFromRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null || uri.equals("/") || uri.equals("/api")) {
            return "home";
        }
        
        // Remove API prefix and extract page
        if (uri.startsWith("/api/")) {
            return "api-call";
        }
        
        return uri.replaceFirst("^/", "").replaceAll("/.*", "");
    }
    @PostConstruct
    public void init() {
        log.info("====== VisitorController created successfully ======");
    }
}