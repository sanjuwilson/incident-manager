package com.manage.notification.notification_service.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification,UUID> {
    Optional<Notification> findById(UUID id);
    List<Notification> findByIncidentId(UUID incidentId);
    List<Notification> findByStatus(NotificationStatus status);
    List<Notification> findByIncidentIdAndStatus(UUID incidentId, NotificationStatus status);
}
