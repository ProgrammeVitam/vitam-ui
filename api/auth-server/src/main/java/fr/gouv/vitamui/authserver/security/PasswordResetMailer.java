/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.authserver.security;

import fr.gouv.vitamui.authserver.config.AuthServerProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Sends the password-reset email. Kept inline (no template engine) — the message is short, the SPA
 * carries the branding, and adding Thymeleaf just for this would drag more configuration for no gain.
 */
@Service
public class PasswordResetMailer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetMailer.class);

    private final JavaMailSender mailSender;
    private final AuthServerProperties.PasswordReset settings;

    public PasswordResetMailer(JavaMailSender mailSender, AuthServerProperties properties) {
        this.mailSender = mailSender;
        this.settings = properties.getPasswordReset();
    }

    /**
     * Sends a reset link to {@code recipient}. Failures are logged and re-thrown so the controller
     * can surface a 500 — the reset flow cannot progress if we can't deliver the mail.
     */
    public void send(String recipient, String resetLink, long ttlMinutes) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
        helper.setFrom(settings.getFrom());
        helper.setTo(recipient);
        helper.setSubject("Vitam-UI · Réinitialisation de votre mot de passe");
        helper.setText(buildTextBody(resetLink, ttlMinutes), buildHtmlBody(resetLink, ttlMinutes));
        mailSender.send(message);
        LOGGER.info("Password-reset email sent to {}", recipient);
    }

    private static String buildTextBody(String link, long ttlMinutes) {
        return (
            "Bonjour,\n\n" +
            "Vous (ou quelqu'un d'autre) avez demandé la réinitialisation de votre mot de passe Vitam-UI.\n" +
            "Pour choisir un nouveau mot de passe, ouvrez ce lien dans les " + ttlMinutes + " prochaines minutes :\n\n" +
            link + "\n\n" +
            "Si vous n'êtes pas à l'origine de cette demande, ignorez ce message — votre mot de passe reste inchangé.\n\n" +
            "— Vitam-UI"
        );
    }

    private static String buildHtmlBody(String link, long ttlMinutes) {
        return (
            "<!doctype html><html lang=\"fr\"><body style=\"font-family:Arial,sans-serif;color:#111;\">" +
            "<p>Bonjour,</p>" +
            "<p>Vous (ou quelqu'un d'autre) avez demandé la réinitialisation de votre mot de passe Vitam-UI.</p>" +
            "<p>Pour choisir un nouveau mot de passe, cliquez sur le lien ci-dessous dans les <b>" + ttlMinutes +
            " prochaines minutes</b> :</p>" +
            "<p><a href=\"" + link + "\" style=\"display:inline-block;padding:0.6rem 1rem;background:#1e40af;color:#fff;" +
            "text-decoration:none;border-radius:6px;\">Réinitialiser mon mot de passe</a></p>" +
            "<p style=\"color:#666;font-size:0.85rem;\">Si le bouton ne fonctionne pas, copiez-collez cette adresse " +
            "dans votre navigateur : <br/>" + link + "</p>" +
            "<p style=\"color:#666;font-size:0.85rem;\">Si vous n'êtes pas à l'origine de cette demande, ignorez ce " +
            "message — votre mot de passe reste inchangé.</p>" +
            "<p>— Vitam-UI</p>" +
            "</body></html>"
        );
    }
}
