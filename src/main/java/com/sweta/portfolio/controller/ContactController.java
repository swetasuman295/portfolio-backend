package com.sweta.portfolio.controller;

import com.sweta.portfolio.dto.ContactDTO;
import com.sweta.portfolio.entity.Contact;
import com.sweta.portfolio.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.sweta.portfolio.dto.ContactResponseDTO;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")  // Allow Angular app
@Tag(name = "Contact Management", description = "APIs for managing portfolio contacts")
public class ContactController {

    private final ContactService contactService;

    /**
     * Submit a new contact form
     * POST /api/contacts
     */
    @PostMapping
    @Operation(summary = "Submit a new contact",
            description = "Process a contact form submission with automatic priority detection")
    @ApiResponse(responseCode = "201", description = "Contact created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    public ResponseEntity<Object> submitContact(
            @Valid @RequestBody ContactDTO contactDTO,
            BindingResult bindingResult,
            @RequestHeader(value = "X-Real-IP", required = false) String ipAddress,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }
        
        try {
        	ContactResponseDTO response = contactService.processContact(
                    contactDTO,
                    ipAddress != null ? ipAddress : "unknown",
                    userAgent != null ? userAgent : "unknown"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error processing contact", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process contact"));
        }
    }


    /**
     * Get all contacts with pagination
     * GET /api/contacts?page=0&size=10&status=NEW&priority=HIGH
     */
    @GetMapping
    @Operation(summary = "Get all contacts",
            description = "Retrieve contacts with optional filtering by status and priority")
    public ResponseEntity<Page<Contact>> getAllContacts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching contacts - page: {}, size: {}, status: {}, priority: {}",
                page, size, status, priority);

        Page<Contact> contacts = contactService.getAllContacts(status, priority, page, size);
        return ResponseEntity.ok(contacts);
    }

    /**
     * Get a specific contact by ID
     * GET /api/contacts/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get contact by ID", description = "Retrieve a specific contact")
    public ResponseEntity<Contact> getContact(@PathVariable String id) {
        log.info("Fetching contact with ID: {}", id);
        Contact contact = contactService.getContactById(id);
        return ResponseEntity.ok(contact);
    }

    /**
     * Get contact analytics
     * GET /api/contacts/analytics
     */
    @GetMapping("/analytics")
    @Operation(summary = "Get contact analytics",
            description = "Get statistics about contacts (total, by status, by priority, etc.)")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        log.info("Fetching contact analytics");
        Map<String, Object> analytics = contactService.getContactAnalytics();
        return ResponseEntity.ok(analytics);
    }

    /**
     * Mark a contact as responded
     * PUT /api/contacts/{id}/respond
     */
    @PutMapping("/{id}/respond")
    @Operation(summary = "Mark contact as responded",
            description = "Update contact status to RESPONDED")
    public ResponseEntity<Map<String, String>> markAsResponded(@PathVariable String id) {
        log.info("Marking contact {} as responded", id);
        contactService.markAsResponded(id);

        Map<String, String> response = Map.of(
                "status", "SUCCESS",
                "message", "Contact marked as responded"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     * GET /api/contacts/health
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the contact service is running")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Contact Management Service",
                "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
}