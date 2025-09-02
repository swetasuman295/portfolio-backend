package com.sweta.portfolio.kafka.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ContactProcessedEvent {
    
    @JsonProperty("eventId")
    private String eventId = UUID.randomUUID().toString();
    
    @JsonProperty("contactId")
    private String contactId;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("analysisResult")
    private String analysisResult;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("eventType")
    private String eventType = "CONTACT_PROCESSED";

    public ContactProcessedEvent(String contactId, String status, String analysisResult) {
        this.contactId = contactId;
        this.status = status;
        this.analysisResult = analysisResult;
        this.timestamp = LocalDateTime.now();
    }
}