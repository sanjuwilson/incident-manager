package com.manage.notification.notification_service.notification;

public enum NotificationStatus {
    PENDING,        // Created but not processed yet
    PROCESSING,     // Currently being sent
    SENT,           // Successfully delivered to provider
    FAILED,         // Failed permanently
    RETRYING,       // Failed but will retry
    CANCELLED       // Manually cancelled
}
