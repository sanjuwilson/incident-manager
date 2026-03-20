package com.manage.incident_service.rules;

import com.manage.incident_service.failures.FailureType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidentRuleService {
    private final IncidentRulesRepository repo;
    private final  IncidentRuleMapper mapper;

    public IncidentRule getIncidentRuleByFailure(FailureType failureType){
        return repo.findIncidentRuleByFailureType(failureType);
    }

    public List<IncidentRuleDto> getAll() {
        return repo.findAll()
                .stream()
                .map(mapper::toIncidentRuleResponse)
                .collect(Collectors.toList());
    }
    public UUID save(IncidentRuleDto dto) {
        return repo.save(mapper.toIncidentRule(dto)).getId();
    }
    public IncidentRuleDto update(UUID id, IncidentRuleDto dto) {
        IncidentRule existing = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rule not found with id: " + id));
        existing.setFailureType(dto.failureType());
        existing.setThresholdCount(dto.thresholdCount());
        existing.setWindowSeconds(dto.windowSeconds());
        existing.setAction(dto.action());
        existing.setSeverity(dto.severity());
        existing.setEnabled(dto.enabled());
        return mapper.toIncidentRuleResponse(repo.save(existing));
    }

    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Rule not found with id: " + id);
        }
        repo.deleteById(id);
    }

}
