package com.manage.incident_service.incidents;

import com.manage.incident_service.failures.FailureRecord;
import com.manage.incident_service.failures.FailureService;
import com.manage.incident_service.failures.FailureType;
import com.manage.incident_service.rules.IncidentRuleDto;
import com.manage.incident_service.rules.IncidentRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/incident")
@RequiredArgsConstructor
public class IncidentController {
    private final IncidentService incidentService;
    private final FailureService failureService;
    @PreAuthorize("hasRole('admin')or hasRole('user')")
    @GetMapping()
    public ResponseEntity<List<IncidentResponse>>getAllIncidents(){
        return ResponseEntity.ok(incidentService.getAll());
    }
    @PreAuthorize("hasRole('admin')or hasRole('user')")
    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(incidentService.getById(id));
    }
    @PatchMapping("/{id}/acknowledge")
    public ResponseEntity<IncidentResponse> acknowledgeIncident(
            @PathVariable UUID id,
            Authentication authentication) {
        return ResponseEntity.ok(incidentService.acknowledge(id, authentication));
    }
    @PreAuthorize("hasRole('admin')")
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<IncidentResponse> resolveIncidents(@PathVariable UUID id){
        return ResponseEntity.ok(incidentService.resolve(id));

    }
    @PreAuthorize("hasRole('admin')")
    @PatchMapping("/{id}/close")
    public ResponseEntity<IncidentResponse> closeIncidents(@PathVariable UUID id){
        return ResponseEntity.ok(incidentService.close(id));

    }







}
