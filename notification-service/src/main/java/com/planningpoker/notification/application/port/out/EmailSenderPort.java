package com.planningpoker.notification.application.port.out;

import java.util.Map;

/**
 * Output port for sending emails via an external mail provider.
 */
public interface EmailSenderPort {

    /**
     * Sends an email using a Thymeleaf template.
     *
     * @param to            recipient email address
     * @param subject       email subject line
     * @param templateName  Thymeleaf template name (without extension)
     * @param variables     template variables
     */
    void sendEmail(String to, String subject, String templateName, Map<String, Object> variables);
}
