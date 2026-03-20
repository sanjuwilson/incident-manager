package com.manage.incident_service.failures.test;

import com.manage.incident_service.failures.FailureRecord;
import com.manage.incident_service.failures.FailureService;
import com.manage.incident_service.failures.FailureType;
import com.manage.incident_service.incidents.Incident;
import com.manage.incident_service.incidents.IncidentRepository;
import com.manage.incident_service.incidents.IncidentStatus;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class IncidentTriggerWithExistingRuleTest {

    @Autowired
    private FailureService failureService;

    @Autowired
    private IncidentRepository incidentRepository;

    @Test
    void shouldTriggerTimeoutIncidentAfterThreeFailures() {

        Instant now = Instant.now();

        // Send 3 TIMEOUT failures within window (60 sec)
        failureService.save(new FailureRecord(
                "order-service",
                FailureType.SERVICE_UNAVAILABLE,
                "POST /place-order",
                "product-service",
                now.minusSeconds(10),
                "req-1"
        ));

        failureService.save(new FailureRecord(
                "order-service",
                FailureType.SERVICE_UNAVAILABLE,
                "POST /place-order",
                "product-service",
                now.minusSeconds(5),
                "req-2"
        ));

        failureService.save(new FailureRecord(
                "order-service",
                FailureType.SERVICE_UNAVAILABLE,
                "POST /place-order",
                "product-service",
                now,
                "req-3"
        ));

        // Should create exactly 1 incident
        assertEquals(1, incidentRepository.count());

        Incident incident = incidentRepository.findAll().get(0);

        assertEquals("order-service", incident.getSourceService());
        assertEquals(FailureType.SERVICE_UNAVAILABLE, incident.getFailureType());
        assertEquals(IncidentStatus.OPEN, incident.getStatus());
    }
}
