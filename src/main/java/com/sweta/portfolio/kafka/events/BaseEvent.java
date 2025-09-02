package com.sweta.portfolio.kafka.events;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.experimental.SuperBuilder;

/**
 * Base event class - All events inherit from this
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class BaseEvent {
    private String eventId = UUID.randomUUID().toString();
    private String eventType;
    private LocalDateTime timestamp = LocalDateTime.now();
    
}
