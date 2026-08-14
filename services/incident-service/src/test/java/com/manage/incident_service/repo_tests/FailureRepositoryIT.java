package com.manage.incident_service.repo_tests;

import com.manage.incident_service.failures.FailureEvents;
import com.manage.incident_service.failures.FailureRepository;
import com.manage.incident_service.failures.FailureType;
import com.manage.incident_service.incidents.Incident;
import com.manage.incident_service.incidents.IncidentRepository;
import com.manage.incident_service.incidents.IncidentStatus;
import com.manage.incident_service.incidents.Severity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FailureRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("incident_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    private FailureRepository failureRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Test
    void countFailures_shouldCountOnlyMatchingTypeServiceAndTimeWindow() {
        Instant now = Instant.parse("2026-01-01T12:00:00Z");
        Instant before = now.minusSeconds(60);

        failureRepository.save(FailureEvents.builder()
                .sourceService("order-service")
                .failureType(FailureType.TIMEOUT)
                .operation("POST /place-order")
                .dependency("product-service")
                .occurredAt(now.minusSeconds(10))
                .correlationId("corr-1")
                .build());

        failureRepository.save(FailureEvents.builder()
                .sourceService("order-service")
                .failureType(FailureType.TIMEOUT)
                .operation("POST /place-order")
                .dependency("product-service")
                .occurredAt(now.minusSeconds(30))
                .correlationId("corr-2")
                .build());

        failureRepository.save(FailureEvents.builder()
                .sourceService("order-service")
                .failureType(FailureType.TIMEOUT)
                .operation("POST /place-order")
                .dependency("product-service")
                .occurredAt(now.minusSeconds(50))
                .correlationId("corr-3")
                .build());

        failureRepository.save(FailureEvents.builder()
                .sourceService("payment-service")
                .failureType(FailureType.TIMEOUT)
                .operation("POST /pay")
                .dependency("bank-service")
                .occurredAt(now.minusSeconds(20))
                .correlationId("corr-4")
                .build());

        failureRepository.flush();

        int count = failureRepository.countAllByFailureTypeAndOccurredAtBetweenAndSourceService(
                FailureType.TIMEOUT,
                before,
                now,
                "order-service"
        );

        assertThat(count).isEqualTo(3);
    }
    @Test
    void checkServiceDoesNotExistWithGivenValues(){
        incidentRepository.save(
                Incident.builder()
                        .sourceService("order-service")
                        .failureType(FailureType.DB_FAILURE)
                        .createdAt(Instant.now())
                        .status(IncidentStatus.OPEN)
                        .severity(Severity.MEDIUM)
                        .title("DB_FAILURE in order-service")
                        .build()
        );
       incidentRepository.flush();

        boolean notExists=incidentRepository.existsBySourceServiceAndFailureTypeAndStatusNot("order-service",FailureType.DB_FAILURE,IncidentStatus.OPEN);
        assertThat(notExists).isEqualTo(false);


    }

    @Test
    void checkServiceDoesNotExistWithGivenValues1(){
        incidentRepository.save(
                Incident.builder()
                        .sourceService("order-service")
                        .failureType(FailureType.DB_FAILURE)
                        .createdAt(Instant.now())
                        .status(IncidentStatus.CLOSED)
                        .severity(Severity.MEDIUM)
                        .title("DB_FAILURE in order-service")
                        .build()
        );
        incidentRepository.flush();

        boolean notExists1=incidentRepository.existsBySourceServiceAndFailureTypeAndStatusNot("order-service",FailureType.DB_FAILURE,IncidentStatus.OPEN);
        assertThat(notExists1).isEqualTo(true);

    }


}