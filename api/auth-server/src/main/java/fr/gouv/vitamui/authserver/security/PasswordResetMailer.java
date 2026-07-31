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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * Sends the password-reset and welcome emails. Templates live under
 * {@code src/main/resources/templates/mail/}: two files per message (an {@code .html} and a
 * {@code .txt} variant), rendered through their respective Thymeleaf engines. Keeps the message
 * body out of the Java source, ready for future i18n and branding tweaks without a redeploy.
 */
@Service
public class PasswordResetMailer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetMailer.class);

    private static final String TEMPLATE_RESET = "password-reset";
    private static final String TEMPLATE_WELCOME = "welcome";

    private final JavaMailSender mailSender;
    private final AuthServerProperties.PasswordReset settings;
    private final SpringTemplateEngine htmlEngine;
    private final SpringTemplateEngine textEngine;

    public PasswordResetMailer(
        JavaMailSender mailSender,
        AuthServerProperties properties,
        @Qualifier("mailHtmlEngine") SpringTemplateEngine htmlEngine,
        @Qualifier("mailTextEngine") SpringTemplateEngine textEngine
    ) {
        this.mailSender = mailSender;
        this.settings = properties.getPasswordReset();
        this.htmlEngine = htmlEngine;
        this.textEngine = textEngine;
    }

    /**
     * Sends the password-reset link to {@code recipient}. Failures are logged and re-thrown so the
     * controller can surface a 500 — the reset flow cannot progress if we can't deliver the mail.
     */
    public void send(String recipient, String resetLink, long ttlMinutes) throws MessagingException {
        Context ctx = new Context();
        ctx.setVariable("resetLink", resetLink);
        ctx.setVariable("ttlMinutes", ttlMinutes);
        sendMultipart(
            recipient,
            "Vitam-UI · Réinitialisation de votre mot de passe",
            textEngine.process(TEMPLATE_RESET, ctx),
            htmlEngine.process(TEMPLATE_RESET, ctx)
        );
        LOGGER.info("Password-reset email sent to {}", recipient);
    }

    /**
     * Sends the welcome/first-connection email. Same channel as {@link #send} but with a template
     * that pitches the flow as "set your password" rather than "reset it", and speaks to the user by
     * name when we have it.
     */
    public void sendWelcome(String recipient, String firstname, String resetLink, long ttlHours) throws MessagingException {
        String hello = (firstname != null && !firstname.isBlank()) ? ("Bonjour " + firstname + ",") : "Bonjour,";
        Context ctx = new Context();
        ctx.setVariable("hello", hello);
        ctx.setVariable("resetLink", resetLink);
        ctx.setVariable("ttlHours", ttlHours);
        sendMultipart(
            recipient,
            "Vitam-UI · Bienvenue — choisissez votre mot de passe",
            textEngine.process(TEMPLATE_WELCOME, ctx),
            htmlEngine.process(TEMPLATE_WELCOME, ctx)
        );
        LOGGER.info("Welcome email sent to {}", recipient);
    }

    private void sendMultipart(String recipient, String subject, String text, String html) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
        helper.setFrom(settings.getFrom());
        helper.setTo(recipient);
        helper.setSubject(subject);
        helper.setText(text, html);
        mailSender.send(message);
    }
}
