package com.manage.incident_service.incidents;

import com.manage.incident_service.failures.FailureType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidentService {
    private final IncidentRepository incidentRepository;
    private final IncidentMapper incidentMapper;

    public boolean existsActiveIncidentByFailureTypeAndService(FailureType type,String service) {
        return incidentRepository.existsBySourceServiceAndFailureTypeAndStatusNot(service,type,IncidentStatus.CLOSED);
    }
    public Incident save(Incident incident){
        return incidentRepository.save(incident);
    }
    public List<IncidentResponse> getAll(){
        return incidentRepository.findAll().stream().map(incidentMapper::toIncidentResponse).collect(Collectors.toList());
    }

    public IncidentResponse getById(UUID id) {
        return incidentMapper.toIncidentResponse(incidentRepository.findIncidentById(id));
    }
    private Incident getByIdPrivate(UUID id) {
        return incidentRepository.findIncidentById(id);
    }

    public IncidentResponse acknowledge(UUID id, Authentication authentication) {
        Incident incident = this.getByIdPrivate(id);
        incident.setStatus(IncidentStatus.ACKNOWLEDGED);
        incident.setAssignedTo(authentication.getName());
        incident.setAcknowledgedAt(Instant.now());
        return incidentMapper.toIncidentResponse(this.save(incident));
    }

    public IncidentResponse resolve(UUID id) {
        Incident incident=this.getByIdPrivate(id);
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(Instant.now());
        return incidentMapper.toIncidentResponse(this.save(incident));
    }

    public IncidentResponse close(UUID id) {
        Incident incident=this.getByIdPrivate(id);
        incident.setStatus(IncidentStatus.CLOSED);
        incident.setClosedAt(Instant.now());
        return incidentMapper.toIncidentResponse(this.save(incident));
    }
}
