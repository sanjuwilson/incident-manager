package com.manage.incident_service.incidents;

import com.manage.incident_service.failures.FailureType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IncidentRepository extends JpaRepository <Incident, UUID> {
    boolean existsBySourceServiceAndFailureTypeAndStatusNot(
            String sourceService, FailureType failureType, IncidentStatus status
    );

    Incident findIncidentById(UUID id);


}
