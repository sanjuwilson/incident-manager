package com.manage.incident_service.rules;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/rules")
@RequiredArgsConstructor
public class IncidentRuleController {
    private final IncidentRuleService incidentRuleService;
    @PreAuthorize("hasRole('admin')or hasRole('user')")
    @GetMapping()
    public ResponseEntity<List<IncidentRuleDto>> getAllIncidentRules(){
        return ResponseEntity.ok(incidentRuleService.getAll());
    }
    @PreAuthorize("hasRole('admin')")
    @PostMapping
    public ResponseEntity<UUID>  saveIncidentRules(@RequestBody IncidentRuleDto dto){
        return ResponseEntity.ok(incidentRuleService.save(dto)) ;
    }   @PreAuthorize("hasRole('admin')")

    @PatchMapping("/{id}")
    public ResponseEntity<IncidentRuleDto> updateRule(
            @PathVariable UUID id,
            @RequestBody IncidentRuleDto dto) {
        return ResponseEntity.ok(incidentRuleService.update(id, dto));
    }
    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID id) {
        incidentRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
