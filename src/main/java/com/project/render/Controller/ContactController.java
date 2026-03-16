package com.project.render.Controller;

import com.project.render.DTO.ContactRequest;
import com.project.render.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:5173", "https://salon-frontend-vercel-project.vercel.app"})
@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired
    private EmailService emailService;

    private final String adminEmail = System.getenv("ADMIN_EMAIL");

    @PostMapping("/send")
    @ResponseStatus(HttpStatus.OK)
    public String sendContactMessage(@RequestBody ContactRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone is required");
        }

        if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject is required");
        }

        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Message is required");
        }

        if (adminEmail == null || adminEmail.isEmpty()) {
            throw new RuntimeException("ADMIN_EMAIL is not set in environment");
        }

        emailService.sendContactEmailToAdmin(
                adminEmail,
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getSubject(),
                request.getMessage()
        );

        emailService.sendContactAutoReplyToUser(
                request.getEmail(),
                request.getName(),
                request.getSubject(),
                request.getMessage()
        );

        return "Message sent successfully";
    }
}