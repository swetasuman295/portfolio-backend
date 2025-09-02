package com.sweta.portfolio.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sweta.portfolio.dto.ContactDTO;
import com.sweta.portfolio.dto.ContactResponseDTO;
import com.sweta.portfolio.entity.Contact;
import com.sweta.portfolio.kafka.ContactEventProducer;
import com.sweta.portfolio.repository.ContactRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j 
public class ContactService {

    private final ContactRepository contactRepository;
    private final ContactEventProducer eventProducer;
    private final EmailService emailService;

    /**
     * Process a new contact form submission
     */
    @Transactional
    public ContactResponseDTO processContact(ContactDTO dto, String ipAddress, String userAgent) {
        log.info("Processing new contact from: {}", dto.getEmail());

        // Step 1: Create contact entity from DTO
        Contact contact = Contact.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .company(dto.getCompany())
                .message(dto.getMessage())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .status(Contact.ContactStatus.NEW)
                .createdAt(LocalDateTime.now())
                .build();

        // Step 2: Determine priority based on keywords
        contact.setPriority(determinePriority(dto.getMessage()));

        // Step 3: Save to database
        Contact savedContact = contactRepository.saveAndFlush(contact);
        log.info("Contact saved with ID: {}", savedContact.getId());

        // Step 4: Send event to Kafka for async processing
        eventProducer.publishContactSubmittedEvent(savedContact);

        // Step 5: Send email notification if high priority
        if (contact.getPriority() == Contact.Priority.URGENT ||
                contact.getPriority() == Contact.Priority.HIGH) {
            emailService.sendUrgentContactNotification(contact.getEmail(),contact.getName(),contact.getMessage());
        }

        // Step 6: Return response
        Map<String, String> response = new HashMap<>();
        response.put("id", savedContact.getId());
        response.put("status", "SUCCESS");
        response.put("message", "Thank you for reaching out! I'll get back to you within 24-48 hours.");

        return buildSmartResponse(savedContact);
    }
    private ContactResponseDTO buildSmartResponse(Contact contact) {
        String responseTime = determineResponseTime(contact);
        String nextSteps = generateNextSteps(contact);
        String estimatedResponse = calculateEstimatedResponseTime(contact);
        
        return ContactResponseDTO.builder()
                .contactId(contact.getId())
                .status("SUCCESS")
                .message(generatePersonalizedMessage(contact))
                .responseTime(responseTime)
                .priority(contact.getPriority().toString().toLowerCase())
                .nextSteps(nextSteps)
                .estimatedResponse(estimatedResponse)
                .eventStatus("PROCESSING_STARTED")
                .queuePosition(calculateQueuePosition(contact))
                .build();
    }

    /**
     * Generate personalized response message
     */
    private String generatePersonalizedMessage(Contact contact) {
        String name = contact.getName() != null ? contact.getName().split(" ")[0] : "there";
        
        return switch(contact.getPriority()) {
            case URGENT -> String.format("Hi %s! Thanks for your urgent message. I'm prioritizing this and will respond very soon.", name);
            case HIGH -> String.format("Hi %s! Thanks for reaching out. Your message caught my attention and I'll respond quickly.", name);
            case MEDIUM -> String.format("Hi %s! Thanks for your message. I've received it and will get back to you soon.", name);
            case LOW -> String.format("Hi %s! Thanks for reaching out. I've received your message and will respond when I can.", name);
        };
    }

    /**
     * Determine response time based on priority
     */
    private String determineResponseTime(Contact contact) {
        return switch(contact.getPriority()) {
            case URGENT -> "within 2-4 hours";
            case HIGH -> "within 8 hours"; 
            case MEDIUM -> "within 24 hours";
            case LOW -> "within 48 hours";
        };
    }

    /**
     * Generate next steps based on message content
     */
    private String generateNextSteps(Contact contact) {
        String message = contact.getMessage().toLowerCase();
        
        if (message.contains("hiring") || message.contains("job") || message.contains("interview")) {
            return "I'll review your opportunity and send you my latest CV along with my response.";
        } else if (message.contains("project") || message.contains("collaborate")) {
            return "I'll assess the project requirements and get back to you with my availability and approach.";
        } else if (message.contains("meeting") || message.contains("call")) {
            return "I'll check my calendar and propose some meeting times that work for both of us.";
        } else {
            return "I'll review your message carefully and provide a detailed response.";
        }
    }

    /**
     * Calculate estimated response timestamp
     */
    private String calculateEstimatedResponseTime(Contact contact) {
        LocalDateTime estimatedTime = switch(contact.getPriority()) {
            case URGENT -> LocalDateTime.now().plusHours(3);
            case HIGH -> LocalDateTime.now().plusHours(8);
            case MEDIUM -> LocalDateTime.now().plusDays(1);
            case LOW -> LocalDateTime.now().plusDays(2);
        };
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' h:mm a");
        return estimatedTime.format(formatter);
    }

    /**
     * Calculate queue position based on current workload
     */
    private int calculateQueuePosition(Contact contact) {
        long pendingHigherPriority = switch(contact.getPriority()) {
            case URGENT -> 0; // Always first
            case HIGH -> contactRepository.countByStatusAndPriority(
                Contact.ContactStatus.NEW, Contact.Priority.URGENT);
            case MEDIUM -> contactRepository.countByStatusAndPriority(
                Contact.ContactStatus.NEW, Contact.Priority.URGENT) + 
                contactRepository.countByStatusAndPriority(
                Contact.ContactStatus.NEW, Contact.Priority.HIGH);
            case LOW -> contactRepository.countByStatusAndPriority(
                Contact.ContactStatus.NEW, Contact.Priority.URGENT) + 
                contactRepository.countByStatusAndPriority(
                Contact.ContactStatus.NEW, Contact.Priority.HIGH) +
                contactRepository.countByStatusAndPriority(
                Contact.ContactStatus.NEW, Contact.Priority.MEDIUM);
        };
        
        return (int) pendingHigherPriority + 1;
    }

    /**
     * Get all contacts with pagination and filtering
     */
    public Page<Contact> getAllContacts(String status, String priority, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (status != null && priority != null) {
            Contact.ContactStatus contactStatus = Contact.ContactStatus.valueOf(status);
            Contact.Priority contactPriority = Contact.Priority.valueOf(priority);
            return contactRepository.findByStatusAndPriority(contactStatus, contactPriority, pageable);
        } else if (status != null) {
            Contact.ContactStatus contactStatus = Contact.ContactStatus.valueOf(status);
            return contactRepository.findByStatus(contactStatus, pageable);
        } else if (priority != null) {
            Contact.Priority contactPriority = Contact.Priority.valueOf(priority);
            return contactRepository.findByPriority(contactPriority, pageable);
        }

        return contactRepository.findAll(pageable);
    }

    /**
     * Get contact by ID
     */
    public Contact getContactById(String id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found with id: " + id));
    }

    /**
     * Get analytics/statistics about contacts
     */
    public Map<String, Object> getContactAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        // Total contacts
        analytics.put("totalContacts", contactRepository.count());

        // Contacts by status
        Map<String, Long> statusCount = new HashMap<>();
        for (Contact.ContactStatus status : Contact.ContactStatus.values()) {
            statusCount.put(status.name(), contactRepository.countByStatus(status));
        }
        analytics.put("contactsByStatus", statusCount);

        // Contacts by priority
        Map<String, Long> priorityCount = new HashMap<>();
        for (Contact.Priority priority : Contact.Priority.values()) {
            priorityCount.put(priority.name(), contactRepository.countByPriority(priority));
        }
        analytics.put("contactsByPriority", priorityCount);

        // Recent contacts (last 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Contact> recentContacts = contactRepository.findRecentContacts(sevenDaysAgo);
        analytics.put("recentContactsCount", recentContacts.size());

        // Unresponded urgent contacts
        List<Contact> urgentUnresponded = contactRepository.findUnrespondedHighPriorityContacts();
        analytics.put("urgentUnrespondedCount", urgentUnresponded.size());

        return analytics;
    }

    /**
     * Mark a contact as responded
     */
    @Transactional
    public void markAsResponded(String id) {
        Contact contact = getContactById(id);
        contact.setStatus(Contact.ContactStatus.RESPONDED);
        contact.setProcessedAt(LocalDateTime.now());
        contactRepository.save(contact);

        log.info("Contact {} marked as responded", id);
    }

    /**
     * Determine priority based on message keywords
     */
    private Contact.Priority determinePriority(String message) {
        String lowerMessage = message.toLowerCase();

        // Check for urgent keywords
        String[] urgentKeywords = {"urgent", "asap", "immediately", "hiring", "job opportunity"};
        for (String keyword : urgentKeywords) {
            if (lowerMessage.contains(keyword)) {
                return Contact.Priority.URGENT;
            }
        }

        // Check for high priority keywords
        String[] highKeywords = {"interested", "project", "collaborate", "interview"};
        for (String keyword : highKeywords) {
            if (lowerMessage.contains(keyword)) {
                return Contact.Priority.HIGH;
            }
        }

        // Default to medium
        return Contact.Priority.MEDIUM;
    }
}