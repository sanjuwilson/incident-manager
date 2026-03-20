package com.manage.email;



import com.manage.notification.notification_service.notification.Notification;
import com.manage.notification.notification_service.notification.NotificationService;
import com.manage.notification.notification_service.notification.NotificationStatus;
import com.manage.notification.notification_service.notification.SendNotification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.manage.notification.notification_service.notification.NotificationStatus.PROCESSING;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final NotificationService notificationService;

    @Async
    public void sendNotification(UUID id) throws MessagingException {
        Notification notification = notificationService.markProcessing(id);

        // Prepare variables for template
        Context context = new Context();
        context.setVariable("title", notification.getTitle());
        context.setVariable("recipientName", notification.getRecipient());
        context.setVariable("incidentId", notification.getIncidentId());
        context.setVariable("severity", notification.getSeverity());
        context.setVariable("incidentStatus", notification.getIncidentStatus());
        context.setVariable("sourceService", notification.getSourceService());
        context.setVariable("failureReason", notification.getFailureReason());
        context.setVariable("sentAt", notification.getSentAt());

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED, StandardCharsets.UTF_8.name());
            helper.setFrom("noreply@yourcompany.com"); // replace with default sender if needed
            helper.setTo(notification.getRecipient());
            helper.setSubject(notification.getTitle());
            String htmlTemplate = templateEngine.process("notification", context);
            helper.setText(htmlTemplate, true);
            mailSender.send(mimeMessage);
            notificationService.markSent(id);
            log.info("Email sent to {}", notification.getRecipient());
        } catch (Exception e) {
            log.warn("Cannot send email to {}", notification.getRecipient(), e);
            notificationService.markFailed(id, e.getMessage());
        }
    }



}
