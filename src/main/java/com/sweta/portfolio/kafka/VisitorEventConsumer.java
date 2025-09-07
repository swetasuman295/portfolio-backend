package com.sweta.portfolio.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.sweta.portfolio.dto.LiveStatsDTO;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!ci")
public class VisitorEventConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    
    // Counters for live statistics
    private final AtomicInteger activeViewers = new AtomicInteger(0);
    private final AtomicLong profileViews = new AtomicLong(247);
    
    // Track unique countries - thread-safe Set
    private final Set<String> uniqueCountries = ConcurrentHashMap.newKeySet();
    
    // Track active sessions for managing active viewer count
    private final Set<String> activeSessions = ConcurrentHashMap.newKeySet();

    @KafkaListener(
        topics = "${kafka.topics.visitor-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeVisitorEvent(JsonNode event) {
        try {
            String eventType = event.path("eventType").asText();
            log.info("<<<<<< CONSUMER: Received event of type '{}'", eventType);

            if ("VISITOR_SESSION".equals(eventType)) {
                String sessionId = event.path("sessionId").asText();
                String location = event.path("location").asText();
                
                // Extract country from location string (e.g., "Amsterdam, Netherlands" -> "Netherlands")
                String country = extractCountryFromLocation(location);
                
                // Track the country if it's new
                if (country != null && !country.isEmpty()) {
                    uniqueCountries.add(country);
                    log.debug("Added country: {}. Total unique countries: {}", country, uniqueCountries.size());
                }
                
                // Track active sessions
                boolean isNewSession = activeSessions.add(sessionId);
                
                // Only increment active viewers for new sessions
                int currentViewers;
                if (isNewSession) {
                    currentViewers = activeViewers.incrementAndGet();
                    log.info("New session detected: {}. Active viewers: {}", sessionId, currentViewers);
                } else {
                    currentViewers = activeViewers.get();
                    log.debug("Existing session: {}. Active viewers unchanged: {}", sessionId, currentViewers);
                }
                
                // Always increment total profile views
                long totalViews = profileViews.incrementAndGet();
                
                // Create DTO with actual country count
                LiveStatsDTO statsUpdate = new LiveStatsDTO(
                    currentViewers, 
                    uniqueCountries.size(),  // Real country count!
                    (int) totalViews
                );

                log.info("Broadcasting live stats - Viewers: {}, Countries: {}, Total Views: {}", 
                    currentViewers, uniqueCountries.size(), totalViews);
                    
                messagingTemplate.convertAndSend("/topic/live-stats", statsUpdate);
            }
            
            // Handle PAGE_VIEW events to potentially decrement active viewers
            else if ("PAGE_VIEW".equals(eventType)) {
                String page = event.path("page").asText();
                
                // If user navigates away or closes tab, you could decrement active viewers
                if ("exit".equals(page) || "close".equals(page)) {
                    String sessionId = event.path("sessionId").asText();
                    if (activeSessions.remove(sessionId)) {
                        int currentViewers = activeViewers.decrementAndGet();
                        log.info("Session ended: {}. Active viewers: {}", sessionId, currentViewers);
                        
                        // Broadcast updated stats
                        LiveStatsDTO statsUpdate = new LiveStatsDTO(
                            currentViewers,
                            uniqueCountries.size(),
                            (int) profileViews.get()
                        );
                        messagingTemplate.convertAndSend("/topic/live-stats", statsUpdate);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error processing visitor event from Kafka", e);
        }
    }
    
    /**
     * Extract country from location string
     * Examples: 
     * "Amsterdam, Netherlands" -> "Netherlands"
     * "Local Development" -> "Local"
     * "New York, United States" -> "United States"
     */
    private String extractCountryFromLocation(String location) {
        if (location == null || location.isEmpty()) {
            return "Unknown";
        }
        
        // Handle special cases
        if (location.equals("Local Development")) {
            return "Local";
        }
        
        // Split by comma and take the last part (usually the country)
        String[] parts = location.split(",");
        if (parts.length > 1) {
            return parts[parts.length - 1].trim();
        }
        
        // If no comma, return the whole location
        return location.trim();
    }
    
    /**
     * Get current statistics - useful for initial load or debugging
     */
    public LiveStatsDTO getCurrentStats() {
        return new LiveStatsDTO(
            activeViewers.get(),
            uniqueCountries.size(),
            (int) profileViews.get()
        );
    }
    
    /**
     * Reset statistics - useful for testing or scheduled resets
     */
    public void resetStats() {
        activeViewers.set(0);
        activeSessions.clear();
        uniqueCountries.clear();
        profileViews.set(0);
        log.info("Statistics reset");
    }
    
    @PostConstruct
    public void init() {
        log.info("🚀 VisitorEventConsumer STARTED!");
        log.info("📊 Initial stats - Active: {}, Countries: {}, Total views: {}", 
            activeViewers.get(), uniqueCountries.size(), profileViews.get());
            
        // Initialize with some default countries for demo purposes (optional)
        // Remove these lines for production
        uniqueCountries.add("Netherlands");
        uniqueCountries.add("United States");
        uniqueCountries.add("Germany");
        log.info("Initialized with {} demo countries", uniqueCountries.size());
    }
}