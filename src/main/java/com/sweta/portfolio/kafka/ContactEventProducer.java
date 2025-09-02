package com.sweta.portfolio.kafka;

import com.sweta.portfolio.entity.Contact;
import com.sweta.portfolio.kafka.events.ContactSubmittedEvent;
import com.sweta.portfolio.kafka.events.ContactProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContactEventProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topics.contact-events}")
    private String contactEventsTopic;
    
    /**
     * Publish event when a new contact is submitted
     */
    public void publishContactSubmittedEvent(Contact contact) {
        // Create the event
        ContactSubmittedEvent event = new ContactSubmittedEvent(
            contact.getId(),
            contact.getEmail(),
            contact.getName(),
            contact.getCompany(),
            contact.getMessage(),
            contact.getPriority() != null ? contact.getPriority().toString() : "MEDIUM"
        );
        
        // Send to Kafka
        log.info("Publishing ContactSubmittedEvent for contact: {}", contact.getId());
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(contactEventsTopic, contact.getId(), event);
        
        // Handle success/failure
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully published event with key: {} to partition: {} at offset: {}", 
                    contact.getId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish event for contact: {}", contact.getId(), ex);
            }
        });
    }
    
    /**
     * Publish event when contact is processed
     */
    public void publishContactProcessedEvent(String contactId, String status, String analysisResult) {
        ContactProcessedEvent event = new ContactProcessedEvent(contactId, status, analysisResult);
        
        log.info("Publishing ContactProcessedEvent for contact: {}", contactId);
        
        kafkaTemplate.send(contactEventsTopic, contactId, event);
    }
    
    /**
     * Generic method to publish any event
     */
    public void publishEvent(String topic, String key, Object event) {
        log.debug("Publishing event to topic: {} with key: {}", topic, key);
        
        kafkaTemplate.send(topic, key, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Error publishing event to topic: {}", topic, ex);
                }
            });
    }
}
