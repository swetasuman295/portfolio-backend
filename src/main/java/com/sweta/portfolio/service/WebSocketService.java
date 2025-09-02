package com.sweta.portfolio.service;

import com.sweta.portfolio.entity.Contact;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Broadcast contact update to all connected clients
     */
    public void broadcastContactUpdate(Contact contact) {
        Map<String, Object> update = new HashMap<>();
        update.put("id", contact.getId());
        update.put("name", contact.getName());
        update.put("email", contact.getEmail());
        update.put("status", contact.getStatus().toString());
        update.put("priority", contact.getPriority() != null ? contact.getPriority().toString() : "MEDIUM");
        update.put("timestamp", LocalDateTime.now().toString());
        update.put("type", "CONTACT_UPDATE");
        
        log.info("Broadcasting contact update for: {}", contact.getId());
        
        // Send to all subscribers of /topic/contacts
        messagingTemplate.convertAndSend("/topic/contacts", update);
    }
    
    /**
     * Send notification to all admin users
     */
    public void sendAdminNotification(String title, String message, String severity) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("message", message);
        notification.put("severity", severity); // INFO, WARNING, ERROR, SUCCESS
        notification.put("timestamp", LocalDateTime.now().toString());
        
        log.info("Sending admin notification: {}", title);
        
        // Broadcast to all admins
        messagingTemplate.convertAndSend("/topic/admin-notifications", notification);
    }
    
    /**
     * Send visitor activity update
     */
    public void broadcastVisitorActivity(String page, String action) {
        Map<String, Object> activity = new HashMap<>();
        activity.put("page", page);
        activity.put("action", action);
        activity.put("timestamp", LocalDateTime.now().toString());
        activity.put("type", "VISITOR_ACTIVITY");
        
        log.debug("Broadcasting visitor activity: {} on {}", action, page);
        
        messagingTemplate.convertAndSend("/topic/visitor-activity", activity);
    }
    
    /**
     * Send analytics update
     */
    public void broadcastAnalyticsUpdate(Map<String, Object> analytics) {
        analytics.put("timestamp", LocalDateTime.now().toString());
        analytics.put("type", "ANALYTICS_UPDATE");
        
        log.info("Broadcasting analytics update");
        
        messagingTemplate.convertAndSend("/topic/analytics", analytics);
    }
    
    /**
     * Send a private message to a specific user
     */
    public void sendPrivateMessage(String userId, String message) {
        Map<String, Object> privateMsg = new HashMap<>();
        privateMsg.put("message", message);
        privateMsg.put("timestamp", LocalDateTime.now().toString());
        privateMsg.put("type", "PRIVATE_MESSAGE");
        
        log.info("Sending private message to user: {}", userId);
        
        // Send to specific user
        messagingTemplate.convertAndSendToUser(userId, "/queue/messages", privateMsg);
    }
}