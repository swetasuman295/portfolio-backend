package com.sweta.portfolio.service;

import com.sweta.portfolio.entity.Contact;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:swetasuman295@gmail.com}")
    private String fromEmail;
    
    private final String toEmail = "swetasuman295@gmail.com"; 
    
    /**
     * Send notification for urgent contacts
     */
    public void sendUrgentContactNotification(String contactEmail, String contactName, String message) {
        try {
            String subject = "🚨 URGENT: New High-Priority Contact from " + contactName;
            
            String body = buildEmailBody(contactName, contactEmail, message, "URGENT");
            
            log.info("=== EMAIL NOTIFICATION ===");
            log.info("To: {}", toEmail);
            log.info("Subject: {}", subject);
            log.info("Body: {}", body);
            log.info("========================");
            
            sendHtmlEmail(toEmail, subject, body);
            
        } catch (Exception e) {
            log.error("Failed to send urgent contact notification", e);
        }
    }
    
    /**
     * Send notification for any new contact
     */
    public void sendNewContactNotification(Contact contact) {
        try {
            String subject = "New Portfolio Contact: " + contact.getName();
            
            String body = buildEmailBody(
                contact.getName(),
                contact.getEmail(),
                contact.getMessage(),
                contact.getPriority() != null ? contact.getPriority().toString() : "MEDIUM"
            );
            
            log.info("=== EMAIL NOTIFICATION ===");
            log.info("To: {}", toEmail);
            log.info("Subject: {}", subject);
            log.info("========================");
            
           
             sendHtmlEmail(toEmail, subject, body);
            
        } catch (Exception e) {
            log.error("Failed to send contact notification", e);
        }
    }
    
    /**
     * Send simple text email (for testing)
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            // For development, just log
            log.info("Sending email - To: {}, Subject: {}, Text: {}", to, subject, text);
            
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            
            
        } catch (Exception e) {
            log.error("Failed to send email", e);
        }
    }
    
    /**
     * Send HTML email (for production)
     */
    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML
            
            mailSender.send(message);
            
            
            //log.info("HTML email would be sent to: {}", to);
            
        } catch (Exception e) {
            log.error("Failed to send HTML email", e);
            throw new RuntimeException("Email sending failed", e);
        }
    }
    
    /**
     * Build nice HTML email body
     */
    private String buildEmailBody(String name, String email, String message, String priority) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #333;">New Contact from Portfolio</h2>
                <div style="background: #f5f5f5; padding: 20px; border-radius: 5px;">
                    <p><strong>Name:</strong> %s</p>
                    <p><strong>Email:</strong> %s</p>
                    <p><strong>Priority:</strong> <span style="color: %s;">%s</span></p>
                    <p><strong>Message:</strong></p>
                    <div style="background: white; padding: 15px; border-left: 3px solid #4CAF50;">
                        %s
                    </div>
                </div>
                <p style="margin-top: 20px;">
                    <a href="http://localhost:8081/api/contacts" 
                       style="background: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                        View in Dashboard
                    </a>
                </p>
            </body>
            </html>
            """,
            name,
            email,
            getPriorityColor(priority),
            priority,
            message
        );
    }
    
    private String getPriorityColor(String priority) {
        return switch (priority) {
            case "URGENT" -> "#ff0000";
            case "HIGH" -> "#ff9800";
            case "MEDIUM" -> "#2196F3";
            default -> "#4CAF50";
        };
    }
}