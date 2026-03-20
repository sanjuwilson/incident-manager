package com.manage.incident_service.rules;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class IncidentRuleMapper {
    public IncidentRuleDto toIncidentRuleResponse(IncidentRule rule) {
        return new IncidentRuleDto(
                rule.getId(),
                rule.getFailureType(),
                rule.getThresholdCount(),
                rule.getWindowSeconds(),
                rule.getAction(),
                rule.getSeverity(),
                rule.isEnabled(),
                rule.getCreatedAt()
        );
    }
    public IncidentRule toIncidentRule(IncidentRuleDto dto) {
        return IncidentRule.builder()
                .failureType(dto.failureType())
                .thresholdCount(dto.thresholdCount())
                .windowSeconds(dto.windowSeconds())
                .action(dto.action())
                .severity(dto.severity())
                .enabled(dto.enabled())
                .createdAt(Instant.now())
                .build();
    }
}