package de.upteams.tasktracker.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for email sending
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${app.base-url}")
    private String baseUrl;

    private final EmailSender emailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendConfirmationEmail(String sentTo, String confirmationCode) {
        String confirmationLink = "%s/api/v1/users/confirm-redirect/%s".formatted(baseUrl, confirmationCode);

        Map<String, Object> model = Map.of(
                "link", confirmationLink
        );

        String htmlContent = templateEngine.generateHtml("confirm_registration_mail.ftlh", model);
        emailSender.sendEmail(sentTo, "Confirm your registration", htmlContent);
    }

    @Async
    public void sendResetPasswordEmail(String sentTo, String token) {
        String resetLink = "%s/reset-password?token=%s".formatted(baseUrl, token);

        Map<String, Object> model = Map.of(
                "link", resetLink
        );

        String htmlContent = templateEngine.generateHtml("reset_password_mail.ftlh", model);
        emailSender.sendEmail(sentTo, "Reset your password", htmlContent);
    }
}
