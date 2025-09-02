package com.sweta.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.*;

@Entity
@Table(name = "contacts")
@Data
@Builder
@AllArgsConstructor
public class Contact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String email;
    
    private String company;
    
    @Column(length = 2000)
    private String message;
    
    @Enumerated(EnumType.STRING)
    private ContactStatus status = ContactStatus.NEW;
    
    @Enumerated(EnumType.STRING)
    private Priority priority;
    
    private String ipAddress;
    
    private String userAgent;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    // Constructors
    public Contact() {
        this.createdAt = LocalDateTime.now();
        this.status = ContactStatus.NEW;
    }
    
    public Contact(String name, String email, String company, String message) {
        this();
        this.name = name;
        this.email = email;
        this.company = company;
        this.message = message;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public ContactStatus getStatus() {
        return status;
    }
    
    public void setStatus(ContactStatus status) {
        this.status = status;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    // Enums
    public enum ContactStatus {
        NEW,
        PROCESSING,
        ANALYZED,
        RESPONDED,
        ARCHIVED
    }
    
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }
    
    // Lifecycle callback
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ContactStatus.NEW;
        }
    }
    
    // Builder pattern (manual implementation since Lombok might not be working)
    public static ContactBuilder builder() {
        return new ContactBuilder();
    }
    
    public static class ContactBuilder {
        private String name;
        private String email;
        private String company;
        private String message;
        private String ipAddress;
        private String userAgent;
        private Priority priority;
        
        public ContactBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public ContactBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public ContactBuilder company(String company) {
            this.company = company;
            return this;
        }
        
        public ContactBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        public ContactBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }
        
        public ContactBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }
        
        public ContactBuilder priority(Priority priority) {
            this.priority = priority;
            return this;
        }
        
        public Contact build() {
            Contact contact = new Contact();
            contact.setName(this.name);
            contact.setEmail(this.email);
            contact.setCompany(this.company);
            contact.setMessage(this.message);
            contact.setIpAddress(this.ipAddress);
            contact.setUserAgent(this.userAgent);
            contact.setPriority(this.priority);
            return contact;
        }
    }
}