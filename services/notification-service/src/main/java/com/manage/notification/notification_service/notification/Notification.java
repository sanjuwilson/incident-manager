package com.manage.notification.notification_service.notification;


import com.manage.incident_service.incidents.IncidentStatus;
import com.manage.incident_service.incidents.Severity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Notification {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID incidentId;
    private String title;
    private String recipient;
    @Enumerated(EnumType.STRING)
    private NotificationStatus status; // PENDING, SENT, FAILED
    private int retryCount;
    private String sourceService;
    private String failureReason;
    private Instant createdAt;
    @Enumerated(EnumType.STRING)
    private Severity severity;
    private Instant sentAt;
    @Enumerated(EnumType.STRING)
    private IncidentStatus incidentStatus;
}
