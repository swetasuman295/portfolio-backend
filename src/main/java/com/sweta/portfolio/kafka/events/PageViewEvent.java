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
public class PageViewEvent {
    private String eventId = UUID.randomUUID().toString();
    private String eventType = "PAGE_VIEW";
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private String sessionId;
    private String page;
    private String previousPage;
    private Long timeSpentSeconds;
    private String scrollDepth;
}
