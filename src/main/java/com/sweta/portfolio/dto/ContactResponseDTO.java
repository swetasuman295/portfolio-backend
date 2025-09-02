package com.sweta.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactResponseDTO {
	private String contactId;
    private String status;
    private String message;
    private String responseTime;
    private String priority;
    private String nextSteps;
    private String estimatedResponse;
    private String eventStatus;
    private int queuePosition;
    
}
