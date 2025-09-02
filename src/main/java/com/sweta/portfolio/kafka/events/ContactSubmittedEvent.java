package com.sweta.portfolio.kafka.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a new contact is submitted
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)

public class ContactSubmittedEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("eventId")
    private String eventId = UUID.randomUUID().toString(); 
    
    @JsonProperty("contactId")
    private String contactId;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("company")
    private String company;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("priority")
    private String priority;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("eventType")
    private String eventType = "CONTACT_SUBMITTED";
    
    /**
     * Constructor without timestamp and eventType (they get set automatically)
     * 
     */
    
    public ContactSubmittedEvent(String contactId, String email, String name, 
                                 String company, String message, String priority) {
    	this.eventId = UUID.randomUUID().toString();
    	this.contactId = contactId;
        this.email = email;
        this.name = name;
        this.company = company;
        this.message = message;
        this.priority = priority;
        this.timestamp = LocalDateTime.now();
        this.eventType = "CONTACT_SUBMITTED";
    }
}