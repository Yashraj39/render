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
    private final String url = "https://api.brevo.com/v3/smtp/email";

    public void sendOtpEmail(String to, String otp) {
        validateApiKey();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHeaders();

        String htmlContent = buildOtpEmailTemplate(otp);

        Map<String, Object> body = createBaseBody(to, "SlotMyStyle - Your OTP Code", htmlContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
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
        validateApiKey();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHeaders();

        String htmlContent = buildAdminContactEmailTemplate(name, email, phone, subject, message);

        Map<String, Object> body = createBaseBody(adminEmail, "New Contact Query - " + safe(subject), htmlContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("Brevo response code: " + response.getStatusCode());
            System.out.println("Brevo response body: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Exception while sending contact email to admin: " + adminEmail);
            e.printStackTrace();
        }
    }

    public void sendContactAutoReplyToUser(String to, String name, String subject, String message) {
        validateApiKey();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHeaders();

        String htmlContent = buildUserAutoReplyTemplate(name, subject, message);

        Map<String, Object> body = createBaseBody(to, "We received your query - SlotMyStyle", htmlContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("Brevo response code: " + response.getStatusCode());
            System.out.println("Brevo response body: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Exception while sending auto reply to user: " + to);
            e.printStackTrace();
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);
        return headers;
    }

    private Map<String, Object> createBaseBody(String to, String subject, String htmlContent) {
        Map<String, Object> body = new HashMap<>();

        Map<String, String> sender = new HashMap<>();
        sender.put("email", fromEmail);
        sender.put("name", "SlotMyStyle");

        Map<String, String> recipient = new HashMap<>();
        recipient.put("email", to);

        body.put("sender", sender);
        body.put("to", new Object[]{recipient});
        body.put("subject", subject);
        body.put("htmlContent", htmlContent);

        return body;
    }

    private void validateApiKey() {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("Brevo API key is not set!");
        }
    }

    private String buildOtpEmailTemplate(String otp) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>OTP Verification</title>
                </head>
                <body style="margin:0; padding:0; background-color:#f4f7fb; font-family:Arial, Helvetica, sans-serif; color:#1f2937;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color:#f4f7fb; padding:30px 0;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:620px; background:#ffffff; border-radius:20px; overflow:hidden; box-shadow:0 8px 30px rgba(15,23,42,0.08);">
                          <tr>
                            <td style="background:linear-gradient(135deg,#0f172a,#1e293b); padding:28px 32px;">
                              <div style="font-size:14px; color:#cbd5e1; letter-spacing:0.4px;">SlotMyStyle</div>
                              <h1 style="margin:8px 0 0; font-size:28px; line-height:36px; color:#ffffff; font-weight:700;">Your OTP Code</h1>
                            </td>
                          </tr>

                          <tr>
                            <td style="padding:32px;">
                              <p style="margin:0 0 18px; font-size:15px; line-height:24px; color:#475467;">
                                Hello,
                              </p>

                              <p style="margin:0 0 24px; font-size:15px; line-height:24px; color:#475467;">
                                Use the OTP below to verify your SlotMyStyle account.
                              </p>

                              <div style="margin:0 auto 24px; max-width:220px; background:#f8fafc; border:1px solid #e5e7eb; border-radius:16px; text-align:center; padding:18px 20px;">
                                <div style="font-size:32px; font-weight:700; letter-spacing:6px; color:#111827;">%s</div>
                              </div>

                              <div style="background:#eff6ff; border:1px solid #dbeafe; border-radius:14px; padding:16px; margin-bottom:24px;">
                                <p style="margin:0; font-size:14px; line-height:22px; color:#1d4ed8;">
                                  This OTP is valid for <strong>5 minutes</strong>.
                                </p>
                              </div>

                              <p style="margin:0; font-size:13px; line-height:22px; color:#667085;">
                                If you did not request this email, you can safely ignore it.
                              </p>
                            </td>
                          </tr>

                          <tr>
                            <td style="padding:20px 32px; background:#f8fafc; border-top:1px solid #e5e7eb;">
                              <p style="margin:0; font-size:12px; line-height:20px; color:#98a2b3;">
                                SlotMyStyle · Secure account verification
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(escapeHtml(otp));
    }

    private String buildAdminContactEmailTemplate(String name, String email, String phone, String subject, String message) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>New Contact Query</title>
                </head>
                <body style="margin:0; padding:0; background-color:#f4f7fb; font-family:Arial, Helvetica, sans-serif; color:#1f2937;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color:#f4f7fb; padding:30px 0;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:700px; background:#ffffff; border-radius:20px; overflow:hidden; box-shadow:0 8px 30px rgba(15,23,42,0.08);">
                          <tr>
                            <td style="background:linear-gradient(135deg,#0f172a,#1e293b); padding:28px 32px;">
                              <div style="font-size:14px; color:#cbd5e1; letter-spacing:0.4px;">SlotMyStyle Admin</div>
                              <h1 style="margin:8px 0 0; font-size:28px; line-height:36px; color:#ffffff; font-weight:700;">New Contact Form Submission</h1>
                            </td>
                          </tr>

                          <tr>
                            <td style="padding:32px;">
                              <p style="margin:0 0 24px; font-size:15px; line-height:24px; color:#475467;">
                                A new user has submitted a contact form from the website.
                              </p>

                              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;">
                                <tr>
                                  <td style="padding:14px 16px; width:150px; font-size:14px; font-weight:700; color:#344054; background:#f8fafc; border:1px solid #e5e7eb;">Name</td>
                                  <td style="padding:14px 16px; font-size:14px; color:#111827; border:1px solid #e5e7eb;">%s</td>
                                </tr>
                                <tr>
                                  <td style="padding:14px 16px; width:150px; font-size:14px; font-weight:700; color:#344054; background:#f8fafc; border:1px solid #e5e7eb;">Email</td>
                                  <td style="padding:14px 16px; font-size:14px; color:#111827; border:1px solid #e5e7eb;">
                                    <a href="mailto:%s" style="color:#2563eb; text-decoration:none;">%s</a>
                                  </td>
                                </tr>
                                <tr>
                                  <td style="padding:14px 16px; width:150px; font-size:14px; font-weight:700; color:#344054; background:#f8fafc; border:1px solid #e5e7eb;">Phone</td>
                                  <td style="padding:14px 16px; font-size:14px; color:#111827; border:1px solid #e5e7eb;">
                                    <a href="tel:%s" style="color:#2563eb; text-decoration:none;">%s</a>
                                  </td>
                                </tr>
                                <tr>
                                  <td style="padding:14px 16px; width:150px; font-size:14px; font-weight:700; color:#344054; background:#f8fafc; border:1px solid #e5e7eb;">Subject</td>
                                  <td style="padding:14px 16px; font-size:14px; color:#111827; border:1px solid #e5e7eb;">%s</td>
                                </tr>
                              </table>

                              <div style="margin-top:24px;">
                                <div style="font-size:14px; font-weight:700; color:#344054; margin-bottom:10px;">Message</div>
                                <div style="background:#f8fafc; border:1px solid #e5e7eb; border-radius:14px; padding:18px; font-size:14px; line-height:24px; color:#111827; white-space:pre-wrap;">
                                  %s
                                </div>
                              </div>

                              <table role="presentation" cellspacing="0" cellpadding="0" style="margin-top:28px;">
                                <tr>
                                  <td style="padding-right:12px;">
                                    <a href="mailto:%s" style="display:inline-block; background:#111827; color:#ffffff; text-decoration:none; padding:12px 20px; border-radius:12px; font-size:14px; font-weight:700;">
                                      Reply by Email
                                    </a>
                                  </td>
                                  <td>
                                    <a href="tel:%s" style="display:inline-block; background:#eef2ff; color:#1d4ed8; text-decoration:none; padding:12px 20px; border-radius:12px; font-size:14px; font-weight:700;">
                                      Call User
                                    </a>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>

                          <tr>
                            <td style="padding:20px 32px; background:#f8fafc; border-top:1px solid #e5e7eb;">
                              <p style="margin:0; font-size:12px; line-height:20px; color:#98a2b3;">
                                This email was automatically generated from the SlotMyStyle contact form.
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                escapeHtml(name),
                escapeHtml(email), escapeHtml(email),
                escapeHtml(phone), escapeHtml(phone),
                escapeHtml(subject),
                escapeHtml(message),
                escapeHtml(email),
                escapeHtml(phone)
        );
    }

    private String buildUserAutoReplyTemplate(String name, String subject, String message) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>We Received Your Message</title>
                </head>
                <body style="margin:0; padding:0; background-color:#f4f7fb; font-family:Arial, Helvetica, sans-serif; color:#1f2937;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color:#f4f7fb; padding:30px 0;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:680px; background:#ffffff; border-radius:20px; overflow:hidden; box-shadow:0 8px 30px rgba(15,23,42,0.08);">
                          <tr>
                            <td style="background:linear-gradient(135deg,#0f172a,#1e293b); padding:28px 32px;">
                              <div style="font-size:14px; color:#cbd5e1; letter-spacing:0.4px;">SlotMyStyle Support</div>
                              <h1 style="margin:8px 0 0; font-size:28px; line-height:36px; color:#ffffff; font-weight:700;">We Received Your Message</h1>
                            </td>
                          </tr>

                          <tr>
                            <td style="padding:32px;">
                              <p style="margin:0 0 16px; font-size:15px; line-height:24px; color:#475467;">
                                Hello <strong>%s</strong>,
                              </p>

                              <p style="margin:0 0 24px; font-size:15px; line-height:24px; color:#475467;">
                                Thank you for contacting SlotMyStyle. Our team has received your query and will get back to you as soon as possible.
                              </p>

                              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;">
                                <tr>
                                  <td style="padding:14px 16px; width:150px; font-size:14px; font-weight:700; color:#344054; background:#f8fafc; border:1px solid #e5e7eb;">Subject</td>
                                  <td style="padding:14px 16px; font-size:14px; color:#111827; border:1px solid #e5e7eb;">%s</td>
                                </tr>
                              </table>

                              <div style="margin-top:24px;">
                                <div style="font-size:14px; font-weight:700; color:#344054; margin-bottom:10px;">Your Message</div>
                                <div style="background:#f8fafc; border:1px solid #e5e7eb; border-radius:14px; padding:18px; font-size:14px; line-height:24px; color:#111827; white-space:pre-wrap;">
                                  %s
                                </div>
                              </div>

                              <div style="margin-top:24px; background:#ecfdf3; border:1px solid #d1fadf; border-radius:14px; padding:16px;">
                                <p style="margin:0; font-size:14px; line-height:22px; color:#027a48;">
                                  Our support team will review your query and respond soon.
                                </p>
                              </div>
                            </td>
                          </tr>

                          <tr>
                            <td style="padding:20px 32px; background:#f8fafc; border-top:1px solid #e5e7eb;">
                              <p style="margin:0; font-size:12px; line-height:20px; color:#98a2b3;">
                                SlotMyStyle Support Team
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                escapeHtml(name),
                escapeHtml(subject),
                escapeHtml(message)
        );
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}