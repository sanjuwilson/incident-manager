package com.manage.incident_service.incidents;

import org.springframework.stereotype.Service;

@Service
public class IncidentMapper {
    public IncidentResponse toIncidentResponse(Incident incident){
        return new IncidentResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getSourceService(),
                incident.getFailureType(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getCreatedAt(),
                incident.getAcknowledgedAt(),
                incident.getResolvedAt(),
                incident.getClosedAt(),
                incident.getAssignedTo()
        );
    }
}
