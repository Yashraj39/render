package com.project.render.Service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private final String apiKey = System.getenv("BREVO_API_KEY");
    private final String fromEmail = System.getenv("BREVO_MAIL");

    public void sendOtpEmail(String to, String otp) {

        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("Brevo API key is not set!");
        }

        String url = "https://api.brevo.com/v3/smtp/email";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        String htmlContent =
                "<div style='font-family: Arial, sans-serif; padding: 20px;'>" +
                        "<h2 style='color:#2c3e50;'>Your OTP Code</h2>" +
                        "<p>Hello,</p>" +
                        "<p>Your One-Time Password (OTP) for verifying your Glow & Shine Salon account is:</p>" +
                        "<h1 style='background:#f4f4f4; padding:10px; width:120px; text-align:center; " +
                        "border-radius:8px; border:1px solid #ddd;'>" + otp + "</h1>" +
                        "<p>This OTP is valid for <b>5 minutes</b>.</p>" +
                        "<br>" +
                        "<p style='font-size:12px; color:#777;'>If you didn't request this, please ignore this email.</p>" +
                        "<hr>" +
                        "<p style='font-size:11px; color:#999;'>Glow & Shine Salon, Surat, India</p>" +
                        "</div>";

        Map<String, Object> body = new HashMap<>();

        Map<String, String> sender = new HashMap<>();
        sender.put("email", fromEmail);

        Map<String, String> recipient = new HashMap<>();
        recipient.put("email", to);

        body.put("sender", sender);
        body.put("to", new Object[]{recipient});
        body.put("subject", "Glow & Shine Salon – Your OTP Code");
        body.put("htmlContent", htmlContent);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            System.out.println("Brevo response code: " + response.getStatusCode());
            System.out.println("Brevo response body: " + response.getBody());

            if (response.getStatusCode().value() >= 400) {
                System.err.println("Brevo returned error while sending to " + to);
            }

        } catch (Exception e) {
            System.err.println("Exception while sending email to " + to);
            e.printStackTrace();
        }
    }

    public void sendContactEmailToAdmin(String adminEmail, String name, String email, String phone, String subject, String message) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("Brevo API key is not set!");
        }

        String url = "https://api.brevo.com/v3/smtp/email";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        String htmlContent =
                "<div style='font-family: Arial, sans-serif; padding: 20px;'>" +
                        "<h2 style='color:#2c3e50;'>New Contact Form Submission</h2>" +
                        "<p><b>Name:</b> " + name + "</p>" +
                        "<p><b>Email:</b> " + email + "</p>" +
                        "<p><b>Phone:</b> " + phone + "</p>" +
                        "<p><b>Subject:</b> " + subject + "</p>" +
                        "<p><b>Message:</b></p>" +
                        "<div style='background:#f4f4f4; padding:12px; border-radius:8px; border:1px solid #ddd;'>" +
                        message +
                        "</div>" +
                        "<hr>" +
                        "<p style='font-size:11px; color:#999;'>SlotMyStyle Contact Form</p>" +
                        "</div>";

        Map<String, Object> body = new HashMap<>();

        Map<String, String> sender = new HashMap<>();
        sender.put("email", fromEmail);

        Map<String, String> recipient = new HashMap<>();
        recipient.put("email", adminEmail);

        body.put("sender", sender);
        body.put("to", new Object[]{recipient});
        body.put("subject", "New Contact Query - " + subject);
        body.put("htmlContent", htmlContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        restTemplate.postForEntity(url, request, String.class);
    }

    public void sendContactAutoReplyToUser(String to, String name, String subject, String message) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("Brevo API key is not set!");
        }

        String url = "https://api.brevo.com/v3/smtp/email";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        String htmlContent =
                "<div style='font-family: Arial, sans-serif; padding: 20px;'>" +
                        "<h2 style='color:#2c3e50;'>We received your message</h2>" +
                        "<p>Hello <b>" + name + "</b>,</p>" +
                        "<p>Thank you for contacting SlotMyStyle. We have received your query.</p>" +
                        "<p><b>Subject:</b> " + subject + "</p>" +
                        "<p><b>Your Message:</b></p>" +
                        "<div style='background:#f4f4f4; padding:12px; border-radius:8px; border:1px solid #ddd;'>" +
                        message +
                        "</div>" +
                        "<br>" +
                        "<p>Our team will get back to you as soon as possible.</p>" +
                        "<hr>" +
                        "<p style='font-size:11px; color:#999;'>SlotMyStyle Support Team</p>" +
                        "</div>";

        Map<String, Object> body = new HashMap<>();

        Map<String, String> sender = new HashMap<>();
        sender.put("email", fromEmail);

        Map<String, String> recipient = new HashMap<>();
        recipient.put("email", to);

        body.put("sender", sender);
        body.put("to", new Object[]{recipient});
        body.put("subject", "We received your query - SlotMyStyle");
        body.put("htmlContent", htmlContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        restTemplate.postForEntity(url, request, String.class);
    }

}
