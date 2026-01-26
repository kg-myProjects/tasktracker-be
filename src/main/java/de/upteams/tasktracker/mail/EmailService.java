package de.upteams.tasktracker.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${app.base-url}")
    private String baseUrl;

    private final EmailSender emailSender;

    @Async
    public void sendConfirmationEmail(String sentTo, String confirmationCode) {
        String confirmationLink =
                "%s/api/v1/users/confirm/%s".formatted(baseUrl, confirmationCode);

        String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Confirm Registration</title>
                </head>
                <body>
                    <h1>Confirm Registration</h1>
                    <p>Click the link below to confirm your registration:</p>
                    <p>
                        <a href="%s">Confirm Email</a>
                    </p>
                </body>
                </html>
                """.formatted(confirmationLink);

        emailSender.sendEmail(sentTo, "Confirm your registration", htmlContent);
    }

    @Async
    public void sendResetPasswordEmail(String sentTo, String token) {
        String resetLink =
                "%s/reset-password?token=%s".formatted(baseUrl, token);

        String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Password Reset</title>
                </head>
                <body>
                    <h1>Password Reset</h1>
                    <p>You requested a password reset.</p>
                    <p>Click the link below to set a new password:</p>
                    <p>
                        <a href="%s">Reset Password</a>
                    </p>
                    <p>If you did not request this, please ignore this email.</p>
                </body>
                </html>
                """.formatted(resetLink);

        emailSender.sendEmail(sentTo, "Reset your password", htmlContent);
    }
}
