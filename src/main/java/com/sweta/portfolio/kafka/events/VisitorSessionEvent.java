package com.sweta.portfolio.kafka.events;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorSessionEvent {
    private String eventId = UUID.randomUUID().toString();
    private String eventType = "VISITOR_SESSION";
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private String sessionId;
    private String ipAddress;
    private String userAgent;
    private String location;
    private String page;
    private String referrer;
    private String deviceType;
    
    public VisitorSessionEvent(String sessionId, String ipAddress, String userAgent, 
                              String location, String page) {
        this();
        this.sessionId = sessionId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.location = location;
        this.page = page;
        this.deviceType = detectDeviceType(userAgent);
    }
    
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
}