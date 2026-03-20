package com.manage.incident_service.rules;

import com.manage.incident_service.failures.FailureType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IncidentRulesRepository extends JpaRepository<IncidentRule, UUID> {
    IncidentRule findIncidentRuleByFailureType(FailureType failureType);
}
