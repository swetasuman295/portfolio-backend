package com.sweta.portfolio.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;

@Configuration
@Profile("!ci") 
public class KafkaConfig {
    
    
    @Value("${kafka.topics.contact-events}")
    private String contactEventsTopic;
    
    @Value("${kafka.topics.visitor-events}")
    private String visitorEventsTopic;
    
    /**
     * Create Kafka topic for contact events
     * This topic will store all contact-related events
     */
    @Bean
    public NewTopic contactEventsTopic() {
        return TopicBuilder
                .name(contactEventsTopic)
                .partitions(3)  // 3 partitions for parallel processing
                .replicas(1)    // 1 replica (for development)
                .build();
    }
    
    /**
     * Create Kafka topic for visitor tracking events
     * This topic will store visitor activity
     */
    @Bean
    public NewTopic visitorEventsTopic() {
        return TopicBuilder
                .name(visitorEventsTopic)
                .partitions(5)  // More partitions for high-volume visitor data
                .replicas(1)
                .build();
    }
    
    /**
     * Configure JSON message converter
     * This allows Kafka to send/receive JSON messages
     */
    @Bean
    public RecordMessageConverter converter() {
        return new JsonMessageConverter();
    }
}