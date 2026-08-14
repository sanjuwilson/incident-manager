package com.manage.incident_service.unit_tests;

import com.manage.incident_service.failures.*;
import com.manage.incident_service.incidents.Incident;
import com.manage.incident_service.incidents.IncidentNotification;
import com.manage.incident_service.incidents.IncidentService;
import com.manage.incident_service.incidents.Severity;
import com.manage.incident_service.kafka.producer.ProduceNotification;
import com.manage.incident_service.rules.IncidentRule;
import com.manage.incident_service.rules.IncidentRuleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static com.manage.incident_service.rules.IncidentAction.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class FailureServiceTest {

    @Mock
    private ProduceNotification produceNotification;

    @Mock
    private FailureRepository repository;

    @Mock
    private IncidentRuleService incidentRuleService;

    @Mock
    private IncidentService incidentService;

    @InjectMocks
    private FailureService failureService;



    /**
     * INCIDENT NOT CREATED AFTER FAILURE
     * SCENARIO 1- Threshold wasn't reached
     */

    @Test
    void save_whenThresholdNotReached_shouldNotCreateIncident() {
        FailureRecord record = new FailureRecord(
                "order-service",
                FailureType.TIMEOUT,
                "POST /place-order",
                "product-service",
                Instant.now(),
                "corr-1"
        );

        FailureEvents savedEvent = FailureEvents.builder()
                .sourceService("order-service")
                .failureType(FailureType.TIMEOUT)
                .operation("POST /place-order")
                .dependency("product-service")
                .occurredAt(record.occurredAt())
                .correlationId("corr-1")
                .build();

        IncidentRule rule = IncidentRule.builder()
                .id(UUID.randomUUID())
                .failureType(FailureType.TIMEOUT)
                .enabled(true)
                .action(CREATE_INCIDENT)
                .thresholdCount(3)
                .windowSeconds(60)
                .severity(Severity.MEDIUM)
                .build();

        when(repository.save(any(FailureEvents.class))).thenReturn(savedEvent);
        when(incidentRuleService.getIncidentRuleByFailure(FailureType.TIMEOUT)).thenReturn(rule);
        when(incidentService.existsActiveIncidentByFailureTypeAndService(
                FailureType.TIMEOUT,
                "order-service"
        )).thenReturn(false);

        when(repository.countAllByFailureTypeAndOccurredAtBetweenAndSourceService(
                eq(FailureType.TIMEOUT),
                any(Instant.class),
                any(Instant.class),
                eq("order-service")
        )).thenReturn(2);

        failureService.save(record);

        verify(repository).save(any(FailureEvents.class));
        verify(incidentService, never()).save(any(Incident.class));
        verify(produceNotification, never()).sendIncidentNotification(any(IncidentNotification.class));
    }

    //SCENARIO 2- Rules are not enabled
    @Test
    void save_whenRulesAreDisabled_shouldNotCreateIncident(){
        FailureRecord record = new FailureRecord(
                "order-service",
                FailureType.TIMEOUT,
                "POST /place-order",
                "product-service",
                Instant.now(),
                "corr-1"
        );


        FailureEvents savedEvent = FailureEvents.builder()
                .sourceService("order-service")
                .failureType(FailureType.TIMEOUT)
                .operation("POST /place-order")
                .dependency("product-service")
                .occurredAt(record.occurredAt())
                .correlationId("corr-1")
                .build();

        IncidentRule rule = IncidentRule.builder()
                .id(UUID.randomUUID())
                .failureType(FailureType.TIMEOUT)
                .enabled(false)
                .action(CREATE_INCIDENT)
                .thresholdCount(3)
                .windowSeconds(60)
                .severity(Severity.MEDIUM)
                .build();


        when(repository.save(any(FailureEvents.class))).thenReturn(savedEvent);
        when(incidentRuleService.getIncidentRuleByFailure(FailureType.TIMEOUT)).thenReturn(rule);
        failureService.save(record);
        verify(repository).save(any(FailureEvents.class));
        verify(incidentService, never()).save(any(Incident.class));
        verify(produceNotification, never()).sendIncidentNotification(any(IncidentNotification.class));

    }
    //Scenario 3-Incident already exists
    @Test
    void save_doNotCreateDuplicate(){

        FailureRecord record = new FailureRecord(
                "order-service",
                FailureType.TIMEOUT,
                "POST /place-order",
                "product-service",
                Instant.now(),
                "corr-1"
        );

        FailureEvents savedEvent = FailureEvents.builder()
                .sourceService("order-service")
                .failureType(FailureType.TIMEOUT)
                .operation("POST /place-order")
                .dependency("product-service")
                .occurredAt(record.occurredAt())
                .correlationId("corr-1")
                .build();

        IncidentRule rule = IncidentRule.builder()
                .id(UUID.randomUUID())
                .failureType(FailureType.TIMEOUT)
                .enabled(true)
                .action(CREATE_INCIDENT)
                .thresholdCount(3)
                .windowSeconds(60)
                .severity(Severity.MEDIUM)
                .build();



        when(repository.save(any(FailureEvents.class))).thenReturn(savedEvent);
        when(incidentRuleService.getIncidentRuleByFailure(FailureType.TIMEOUT)).thenReturn(rule);
        when(incidentService.existsActiveIncidentByFailureTypeAndService(
                FailureType.TIMEOUT,
                "order-service"
        )).thenReturn(true);
        failureService.save(record);
        verify(repository).save(any(FailureEvents.class));
        verify(incidentService, never()).save(any(Incident.class));
        verify(produceNotification, never()).sendIncidentNotification(any(IncidentNotification.class));



    }
    //Scenario 4-Rules do not require incident creation
    @Test
    void save_createIncidentOnlyWhenRulesSaySo(){
        FailureRecord record = new FailureRecord(
                "order-service",
                FailureType.TIMEOUT,
                "POST /place-order",
                "product-service",
                Instant.now(),
                "corr-1"
        );

        FailureEvents savedEvent = FailureEvents.builder()
                .sourceService("order-service")
                .failureType(FailureType.TIMEOUT)
                .operation("POST /place-order")
                .dependency("product-service")
                .occurredAt(record.occurredAt())
                .correlationId("corr-1")
                .build();

        IncidentRule rule1 = IncidentRule.builder()
                .id(UUID.randomUUID())
                .failureType(FailureType.TIMEOUT)
                .enabled(true)
                .action(LOG_ONLY)
                .thresholdCount(3)
                .windowSeconds(60)
                .severity(Severity.MEDIUM)
                .build();



        when(repository.save(any(FailureEvents.class))).thenReturn(savedEvent);
        when(incidentRuleService.getIncidentRuleByFailure(FailureType.TIMEOUT)).thenReturn(rule1);
        failureService.save(record);
        verify(repository).save(any(FailureEvents.class));
        verify(incidentService, never()).save(any(Incident.class));
        verify(produceNotification, never()).sendIncidentNotification(any(IncidentNotification.class));


    }
    @Test
    void save_whenThresholdReached_shouldCreateIncidentAndSendNotification() {
        FailureRecord record = new FailureRecord(
                "order-service",
                FailureType.TIMEOUT,
                "POST /place-order",
                "product-service",
                Instant.now(),
                "corr-1"
        );

        FailureEvents savedEvent = FailureEvents.builder()
                .sourceService("order-service")
                .failureType(FailureType.TIMEOUT)
                .operation("POST /place-order")
                .dependency("product-service")
                .occurredAt(record.occurredAt())
                .correlationId("corr-1")
                .build();

        IncidentRule rule = IncidentRule.builder()
                .id(UUID.randomUUID())
                .failureType(FailureType.TIMEOUT)
                .enabled(true)
                .action(CREATE_INCIDENT)
                .thresholdCount(3)
                .windowSeconds(60)
                .severity(Severity.MEDIUM)
                .build();

        Incident createdIncident = Incident.builder()
                .id(UUID.randomUUID())
                .sourceService("order-service")
                .ruleId(rule.getId())
                .failureType(FailureType.TIMEOUT)
                .title("TIMEOUT in order-service")
                .severity(Severity.MEDIUM)
                .status(com.manage.incident_service.incidents.IncidentStatus.OPEN)
                .createdAt(Instant.now())
                .build();

        when(repository.save(any(FailureEvents.class))).thenReturn(savedEvent);
        when(incidentRuleService.getIncidentRuleByFailure(FailureType.TIMEOUT)).thenReturn(rule);
        when(incidentService.existsActiveIncidentByFailureTypeAndService(
                FailureType.TIMEOUT,
                "order-service"
        )).thenReturn(false);

        when(repository.countAllByFailureTypeAndOccurredAtBetweenAndSourceService(
                eq(FailureType.TIMEOUT),
                any(Instant.class),
                any(Instant.class),
                eq("order-service")
        )).thenReturn(3);

        when(incidentService.save(any(Incident.class))).thenReturn(createdIncident);

        failureService.save(record);

        verify(repository).save(any(FailureEvents.class));
        verify(incidentService).save(any(Incident.class));
        verify(produceNotification).sendIncidentNotification(any(IncidentNotification.class));
    }

}


