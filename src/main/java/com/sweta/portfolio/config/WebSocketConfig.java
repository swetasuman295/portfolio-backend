package com.sweta.portfolio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    /**
     * Configure message broker
     * This sets up the messaging system
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple broker for broadcasting messages
        // /topic - for broadcasting to all clients
        // /queue - for sending to specific users
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages FROM clients TO server
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }
    
    /**
     * Register WebSocket endpoints
     * This creates the connection point for clients
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Create WebSocket endpoint at /ws
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                    "http://localhost:4200",    // Angular dev server
                    "http://localhost:3000",    // Alternative frontend
                    "https://sweta-portfolio.com", // Production domain
                    "http://localhost",
                    "familyhomecloud.synology.me",
                    "https://swetasuman295.github.io"
                )
                .withSockJS();  // Enable SockJS fallback for browsers that don't support WebSocket
    }
}