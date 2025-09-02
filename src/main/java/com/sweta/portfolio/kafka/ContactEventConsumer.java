package com.sweta.portfolio.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sweta.portfolio.entity.Contact;
import com.sweta.portfolio.kafka.events.ContactProcessedEvent;
import com.sweta.portfolio.kafka.events.ContactSubmittedEvent;
import com.sweta.portfolio.repository.ContactRepository;
import com.sweta.portfolio.service.EmailService;
//import com.sweta.portfolio.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContactEventConsumer {

	private final ContactRepository contactRepository;
	private final EmailService emailService;
	
	private final ContactEventProducer eventProducer;

	// Configure ObjectMapper with JavaTimeModule for LocalDateTime support
	private final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule());

	/**
	 * Listen for contact submitted events
	 */
	@KafkaListener(
			topics = "${kafka.topics.contact-events}",
			groupId = "${spring.kafka.consumer.group-id}",
			containerFactory = "kafkaListenerContainerFactory"
	)
	@Transactional
	public void handleContactEvent( // Changed method name
			ConsumerRecord<String, JsonNode> record,
			@Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
			@Header(KafkaHeaders.OFFSET) long offset) {
		log.info("Received message from partition {} with offset {}: {}",
				partition, offset, record.value());
		try {
			JsonNode eventNode = record.value();
			String eventType = eventNode.path("eventType").asText();
			log.info("Processing event of type: {}", eventType);
			switch (eventType) {
				case "CONTACT_SUBMITTED":
					handleContactSubmittedEvent(eventNode);
					break;
				case "CONTACT_PROCESSED":
					handleContactProcessedEvent(eventNode);
					break;
				default:
					log.warn("Unknown event type: {}", eventType);
			}
		} catch (Exception e) {
			log.error("Error processing contact event", e);
		}
	}

	private void handleContactSubmittedEvent(JsonNode eventNode) {
		try {
			ContactSubmittedEvent event = objectMapper.treeToValue(eventNode, ContactSubmittedEvent.class);
			processContactEvent(event);
		} catch (Exception e) {
			log.error("Error processing ContactSubmittedEvent", e);
		}
	}

	private void handleContactProcessedEvent(JsonNode eventNode) {
		try {
			ContactProcessedEvent event = objectMapper.treeToValue(eventNode, ContactProcessedEvent.class);
			// Handle the processed event - just log for now
			log.info("Contact {} was processed with status: {}", event.getContactId(), event.getStatus());
		} catch (Exception e) {
			log.error("Error processing ContactProcessedEvent", e);
		}
	}

	/**
	 * Process the contact event after successful deserialization
	 */
	private void processContactEvent(ContactSubmittedEvent event) {
		try {
			log.info("Processing ContactSubmittedEvent for contact: {}", event.getContactId());
			// Find the contact in database
			Contact contact = contactRepository.findById(event.getContactId())
					.orElseThrow(() -> new RuntimeException("Contact not found: " + event.getContactId()));
			
			// If the contact is not in the NEW state, it has already been processed.
	        // Log it and stop execution to avoid sending duplicate emails.
			
	        if (contact.getStatus() != Contact.ContactStatus.NEW) {
	            log.warn("Contact {} has already been processed. Current status: {}. Skipping.",
	                    contact.getId(), contact.getStatus());
	            return; // Exit the method
	        }
			// Update contact status
			Contact.ContactStatus previousStatus = contact.getStatus();
			contact.setStatus(Contact.ContactStatus.PROCESSING);
			contact.setProcessedAt(LocalDateTime.now());
			Contact updatedContact = contactRepository.save(contact);
			log.info("Updated contact {} status from {} to {}",
					contact.getId(), previousStatus, contact.getStatus());

			// Perform analysis based on message content
			analyzeAndProcessContact(updatedContact, event);

//			// Send real-time update via WebSocket
//			try {
//				webSocketService.broadcastContactUpdate(updatedContact);
//				log.info("Sent WebSocket update for contact: {}", updatedContact.getId());
//			} catch (Exception e) {
//				log.error("Failed to send WebSocket update", e);
//			}

			// Send email notification for high priority contacts
			try {
				if ("URGENT".equals(event.getPriority()) || "HIGH".equals(event.getPriority())) {
					// Send urgent notification with special formatting
					emailService.sendUrgentContactNotification(event.getEmail(), event.getName(), event.getMessage());
					log.info("Sent urgent notification email for contact: {}", event.getContactId());
				} else {
					// Send regular notification for all other priorities (MEDIUM, LOW)
					emailService.sendNewContactNotification(contact);
					log.info("Sent regular notification email for contact: {}", event.getContactId());
				}
			} catch (Exception e) {
				log.error("Failed to send email notification", e);
			}

			// Publish processed event
			publishProcessedEvent(updatedContact);
		} catch (Exception e) {
			log.error("Failed to process contact event for contactId: {}", event.getContactId(), e);
		}
	}

	/**
	 * Analyze the contact and update accordingly
	 */
	private void analyzeAndProcessContact(Contact contact, ContactSubmittedEvent event) {
		String message = event.getMessage().toLowerCase();
		StringBuilder analysis = new StringBuilder();
		analysis.append("Analysis complete: ");

		// Check for job-related keywords
		if (message.contains("job") || message.contains("hiring") ||
				message.contains("opportunity") || message.contains("position")) {
			analysis.append("JOB_INQUIRY detected. ");
			if (contact.getPriority() != Contact.Priority.URGENT) {
				contact.setPriority(Contact.Priority.HIGH);
			}
		}

		// Check for urgent keywords
		if (message.contains("urgent") || message.contains("asap") ||
				message.contains("immediately")) {
			analysis.append("URGENT request. ");
			contact.setPriority(Contact.Priority.URGENT);
		}

		// Check for technical keywords
		if (message.contains("java") || message.contains("spring") ||
				message.contains("kafka") || message.contains("microservices")) {
			analysis.append("TECHNICAL discussion. ");
		}

		// Update contact with analysis results
		contact.setStatus(Contact.ContactStatus.ANALYZED);
		Contact analyzedContact = contactRepository.save(contact);
		log.info("Analysis for contact {}: {}", contact.getId(), analysis.toString());
	}

	/**
	 * Publish event indicating contact was processed
	 */
	private void publishProcessedEvent(Contact contact) {
		try {
			eventProducer.publishContactProcessedEvent(
					contact.getId(),
					contact.getStatus().toString(),
					"Contact analyzed and processed successfully"
			);
			log.info("Published ContactProcessedEvent for contact: {}", contact.getId());
		} catch (Exception e) {
			log.error("Failed to publish ContactProcessedEvent", e);
		}
	}
}