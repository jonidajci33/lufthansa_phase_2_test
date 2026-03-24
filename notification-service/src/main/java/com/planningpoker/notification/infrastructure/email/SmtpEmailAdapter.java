package com.planningpoker.notification.infrastructure.email;

import com.planningpoker.notification.application.port.out.EmailSenderPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * SMTP-based implementation of {@link EmailSenderPort}.
 * Uses Spring's {@link JavaMailSender} and Thymeleaf for HTML email rendering.
 */
@Component
public class SmtpEmailAdapter implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailAdapter.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String fromAddress;
    private final String fromName;

    public SmtpEmailAdapter(JavaMailSender mailSender,
                            TemplateEngine templateEngine,
                            @Value("${app.notification.from-address}") String fromAddress,
                            @Value("${app.notification.from-name}") String fromName) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
    }

    @Override
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Email sent successfully to={} subject={}", to, subject);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email to " + to, e);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding for email sender name", e);
        }
    }
}
