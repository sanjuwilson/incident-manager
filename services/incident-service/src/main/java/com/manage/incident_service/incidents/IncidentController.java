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
    @PostMapping("/seed")
    public ResponseEntity<Void> seed() {
        Instant now = Instant.now();

        // trigger TIMEOUT (threshold 3)
        for (int i = 0; i < 3; i++) {
            failureService.save(new FailureRecord("order-service", FailureType.TIMEOUT, "POST /place-order", "product-service", now.minusSeconds(i * 5), "req-timeout-" + i));
        }

        // trigger SERVICE_UNAVAILABLE (threshold 2)
        for (int i = 0; i < 2; i++) {
            failureService.save(new FailureRecord("payment-service", FailureType.SERVICE_UNAVAILABLE, "POST /pay", "payment-service", now.minusSeconds(i * 5), "req-svc-" + i));
        }

        // trigger DB_FAILURE (threshold 1)
        failureService.save(new FailureRecord("inventory-service", FailureType.DB_FAILURE, "GET /inventory", "postgres", now, "req-db-1"));

        // trigger AUTH_FAILURE (threshold 5)
        for (int i = 0; i < 5; i++) {
            failureService.save(new FailureRecord("user-service", FailureType.AUTH_FAILURE, "POST /login", "keycloak", now.minusSeconds(i * 5), "req-auth-" + i));
        }

        // trigger second TIMEOUT for different service
        for (int i = 0; i < 3; i++) {
            failureService.save(new FailureRecord("cart-service", FailureType.TIMEOUT, "POST /checkout", "order-service", now.minusSeconds(i * 5), "req-cart-timeout-" + i));
        }

        return ResponseEntity.ok().build();
    }
//    GET    /rules                              - list all rules
//    POST   /rules                              - create rule
//    PATCH  /rules/{id}                         - update rule
//    DELETE /rules/{id}                         - delete rule
//for rules






}
