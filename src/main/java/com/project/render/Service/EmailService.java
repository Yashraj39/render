package com.project.render.Service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final String fromEmail = System.getenv("SENDGRID_MAIL");

    public void sendOtpEmail(String to, String otp) {
        String apiKey = System.getenv("SENDGRID_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("SendGrid API key is not set!");
        }

        Email from = new Email(fromEmail);

        String subject = "Glow & Shine Salon – Your OTP Code";

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

        Email recipient = new Email(to);

        Content content = new Content("text/html", htmlContent);

        Mail mail = new Mail(from, subject, recipient, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            System.out.println("SendGrid response code: " + response.getStatusCode());
            System.out.println("SendGrid response body: " + response.getBody());
            System.out.println("SendGrid response headers: " + response.getHeaders());

            if (response.getStatusCode() >= 400) {
                System.err.println("SendGrid returned an error sending to " + to);
            }
        } catch (Exception e) {
            System.err.println("Exception while sending email to " + to);
            e.printStackTrace();
        }
    }
}
