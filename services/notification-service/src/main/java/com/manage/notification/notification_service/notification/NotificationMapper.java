package com.manage.notification.notification_service.notification;

import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getIncidentId(),
                notification.getTitle(),
                notification.getRecipient(),
                notification.getStatus(),
                notification.getSourceService(),
                notification.getFailureReason(),
                notification.getSeverity(),
                notification.getIncidentStatus(),
                notification.getRetryCount(),
                notification.getCreatedAt(),
                notification.getSentAt()
        );
    }
}