package com.sweta.portfolio.repository;

import com.sweta.portfolio.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, String> {
    
    // Find by status
    Page<Contact> findByStatus(Contact.ContactStatus status, Pageable pageable);
    
    // Find by priority
    Page<Contact> findByPriority(Contact.Priority priority, Pageable pageable);
    
    // Find by both status and priority
    @Query("SELECT c FROM Contact c WHERE c.status = :status AND c.priority = :priority")
    Page<Contact> findByStatusAndPriority(
        @Param("status") Contact.ContactStatus status, 
        @Param("priority") Contact.Priority priority, 
        Pageable pageable
    );
    
    // Count by status
    Long countByStatus(Contact.ContactStatus status);
    
    // Count by priority
    Long countByPriority(Contact.Priority priority);
    Long countByStatusAndPriority(Contact.ContactStatus status, Contact.Priority priority);
    
    // Find recent contacts
    @Query("SELECT c FROM Contact c WHERE c.createdAt >= :startDate ORDER BY c.createdAt DESC")
    List<Contact> findRecentContacts(@Param("startDate") LocalDateTime startDate);
    
    // Find unresponded high priority contacts
    @Query("SELECT c FROM Contact c WHERE c.status != 'RESPONDED' " +
           "AND (c.priority = 'HIGH' OR c.priority = 'URGENT')")
    List<Contact> findUnrespondedHighPriorityContacts();
    
    // Get contacts by company
    @Query("SELECT c.company, COUNT(c) FROM Contact c " +
           "WHERE c.company IS NOT NULL GROUP BY c.company")
    List<Object[]> getContactsByCompany();
}