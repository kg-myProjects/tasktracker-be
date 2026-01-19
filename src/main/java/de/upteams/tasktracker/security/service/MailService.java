package de.upteams.tasktracker.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetMail(String to, String link) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Reset your password");
        message.setText("""
                Hello, you requested a password reset.
                Click the link below: %s
                This link is valid for 30 minutes.
                If you didn't request this, ignore this email.
                """.formatted(link));

        mailSender.send(message);
    }
}
