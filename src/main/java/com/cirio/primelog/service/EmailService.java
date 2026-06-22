package com.cirio.primelog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWaitlistConfirmation(String toEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "PrimeLog");
            helper.setTo(toEmail);
            helper.setSubject("Ya estás en la lista de PrimeLog 🚀");
            helper.setText("""
                <html>
                <body style="font-family:Inter,sans-serif;background:#0f1117;color:#e8eaf0;padding:40px;margin:0;">
                  <div style="max-width:480px;margin:0 auto;background:#181c27;border-radius:16px;padding:40px;border:1px solid rgba(255,255,255,0.08);">
                    <h2 style="color:#5ee3a0;font-size:20px;margin:0 0 16px;">¡Apuntado!</h2>
                    <p style="color:#9ca3af;line-height:1.6;margin:0 0 16px;">
                      Gracias por unirte a la lista de espera de
                      <strong style="color:#e8eaf0;">PrimeLog</strong>.
                    </p>
                    <p style="color:#9ca3af;line-height:1.6;margin:0 0 24px;">
                      Serás de los primeros en acceder cuando abramos el beta.
                      Te avisamos en cuanto esté listo.
                    </p>
                    <p style="color:#6b7280;font-size:13px;margin:0;">— El equipo de PrimeLog</p>
                  </div>
                </body>
                </html>
                """, true);

            mailSender.send(message);
            System.out.println("✅ Email de confirmación enviado a: " + toEmail);

        } catch (Exception e) {
            System.out.println("❌ Error enviando email a " + toEmail + ": " + e.getMessage());
        }
    }

    public void sendInternalNotification(String newEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "PrimeLog");
            helper.setTo(fromEmail); // te llega a ti mismo
            helper.setSubject("🔔 Nuevo registro en PrimeLog: " + newEmail);
            helper.setText("<p>Nuevo email en la waitlist: <strong>" + newEmail + "</strong></p>", true);

            mailSender.send(message);

        } catch (Exception e) {
            System.out.println("❌ Error enviando notificación interna: " + e.getMessage());
        }
    }
}
