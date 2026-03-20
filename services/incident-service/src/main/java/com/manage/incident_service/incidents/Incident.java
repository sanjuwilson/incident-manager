package com.manage.incident_service.incidents;

import com.manage.incident_service.failures.FailureType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incidents")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Incident {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String title;



    @Column(name = "source_service", nullable = false)
    private String sourceService;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_type", nullable = false)
    private FailureType failureType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "closed_at")
    private Instant closedAt;


    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "rule_id")
    private UUID ruleId;


}

