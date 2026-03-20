package com.manage.notification.notification_service.notification;

import com.manage.incident_service.incidents.IncidentNotification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper mapper;

    public Notification save(IncidentNotification notificationDto) {
        String RECIPIENT = "maildev@123";
        return notificationRepository.save(Notification.builder()
                .incidentId(notificationDto.id())
                .incidentStatus(notificationDto.status())
                .createdAt(Instant.now())
                .sourceService(notificationDto.sourceService())
                .title(notificationDto.title())
                .failureReason("The reason for failure is " + notificationDto.failureType())
                .recipient(RECIPIENT)
                .severity(notificationDto.severity())
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .build()
        );
    }

    public Notification getNotificationById(UUID id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No notification with id: " + id));
    }

    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    public Notification markProcessing(UUID id) {
        Notification notification = getNotificationById(id);
        notification.setStatus(NotificationStatus.PROCESSING);
        return this.save(notification);
    }

    public void markSent(UUID id) {
        Notification notification = getNotificationById(id);
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(Instant.now());
        this.save(notification);
    }

    public void markFailed(UUID id, String message) {
        Notification notification = getNotificationById(id);
        notification.setStatus(NotificationStatus.FAILED); // fixed
        notification.setFailureReason(message);
        notification.setRetryCount(notification.getRetryCount() + 1);
        this.save(notification);
    }

    public List<NotificationResponse> getAll(UUID incidentId, NotificationStatus status) {
        List<Notification> results;
        if (incidentId != null && status != null) {
            results = notificationRepository.findByIncidentIdAndStatus(incidentId, status);
        } else if (incidentId != null) {
            results = notificationRepository.findByIncidentId(incidentId);
        } else if (status != null) {
            results = notificationRepository.findByStatus(status);
        } else {
            results = notificationRepository.findAll();
        }
        return results.stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    public NotificationResponse getById(UUID id) {
        return mapper.toResponse(getNotificationById(id));
    }

    public List<NotificationResponse> getByIncidentId(UUID incidentId) {
        return notificationRepository.findByIncidentId(incidentId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public NotificationResponse retry(UUID id) {
        Notification notification = getNotificationById(id);
        if (notification.getStatus() != NotificationStatus.FAILED) {
            throw new IllegalStateException("Only FAILED notifications can be retried");
        }
        notification.setStatus(NotificationStatus.PENDING);
        notification.setRetryCount(notification.getRetryCount() + 1);
        return mapper.toResponse(this.save(notification));
    }
}