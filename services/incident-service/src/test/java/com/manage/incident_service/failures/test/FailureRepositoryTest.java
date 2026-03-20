package com.manage.incident_service.failures.test;

import com.manage.incident_service.failures.FailureType;
import com.manage.incident_service.incidents.Incident;
import com.manage.incident_service.incidents.IncidentRepository;
import com.manage.incident_service.incidents.IncidentStatus;
import com.manage.incident_service.incidents.Severity;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class FailureRepositoryTest {

    @Autowired
    private IncidentRepository repository;

    @Test
    void shouldDetectActiveIncident() {

        repository.save(
                Incident.builder()
                        .sourceService("order-service")
                        .failureType(FailureType.TIMEOUT)
                        .status(IncidentStatus.OPEN)
                        .severity(Severity.HIGH)
                        .createdAt(Instant.now())
                        .title("Timeout")
                        .build()
        );

        boolean exists =
                repository.existsBySourceServiceAndFailureTypeAndStatusNot(
                        "order-service",
                        FailureType.TIMEOUT,
                        IncidentStatus.CLOSED
                );

        assertEquals(true, exists);
    }
}
