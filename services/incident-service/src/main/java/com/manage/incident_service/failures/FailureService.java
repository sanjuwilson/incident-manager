package com.manage.incident_service.failures;

import com.manage.incident_service.incidents.Incident;
import com.manage.incident_service.incidents.IncidentNotification;
import com.manage.incident_service.incidents.IncidentService;
import com.manage.incident_service.incidents.IncidentStatus;
import com.manage.incident_service.kafka.producer.ProduceNotification;
import com.manage.incident_service.rules.IncidentRule;
import com.manage.incident_service.rules.IncidentRuleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.manage.incident_service.rules.IncidentAction.CREATE_INCIDENT;

@Service
@RequiredArgsConstructor
@Slf4j
public class FailureService {
    private final ProduceNotification produceNotification;
    private final FailureRepository repository;
    private final IncidentRuleService incidentRuleService;
    private final IncidentService incidentService;
    @Transactional
    public void save(FailureRecord record) {
        if (repository.existsByCorrelationId(record.correlationId())) {
            log.info("Duplicate failure event ignored. correlationId={}", record.correlationId());
            return;
        }

        FailureEvents event = repository.save(FailureEvents.builder()
                .sourceService(record.sourceService())
                .failureType(record.failureType())
                .operation(record.operation())
                .dependency(record.dependency())
                .occurredAt(record.occurredAt())
                .correlationId(record.correlationId())
                .build());

        applyRules(event);
    }

    private void applyRules(FailureEvents event) {
        FailureType type= event.getFailureType();
        IncidentRule rule=  incidentRuleService.getIncidentRuleByFailure(type);
        if(!rule.isEnabled()){
            return;
        }

        switch (rule.getAction()) {
            case CREATE_INCIDENT -> {
                if (!incidentService.existsActiveIncidentByFailureTypeAndService(type, event.getSourceService())) {
                    Instant now = Instant.now();
                    Instant before = now.minusSeconds(rule.getWindowSeconds());
                    int count = repository.countAllByFailureTypeAndOccurredAtBetweenAndSourceService(
                            type,
                            before
                            , now, event.getSourceService()
                    );
                    if (count >= rule.getThresholdCount()) {
                        Incident incident = incidentService.save(
                                Incident.builder()
                                        .sourceService(event.getSourceService())
                                        .ruleId(rule.getId())
                                        .failureType(type)
                                        .createdAt(Instant.now())
                                        .title(type + " in " + event.getSourceService())
                                        .severity(rule.getSeverity())
                                        .status(IncidentStatus.OPEN)
                                        .build()
                        );
                        log.warn(
                                "Incident CREATED: {} | service={} | severity={}",
                                incident.getId(),
                                event.getSourceService(),
                                rule.getSeverity()
                        );

                        sendNotification(new IncidentNotification(incident.getId(), incident.getTitle(), event.getSourceService(), type, rule.getSeverity(), incident.getStatus(), incident.getCreatedAt(), incident.getRuleId()
                        ));


                    }

                }


            }
            case LOG_ONLY -> log.info("Failure {} in service {} ignored by rule {}",
                    type,
                    event.getSourceService(),
                    rule.getId()); //to do
            case IGNORE -> {
            }
        }

    }
    private void sendNotification(IncidentNotification incidentNotification) {
        try {
            produceNotification.sendIncidentNotification(incidentNotification);
        } catch (Exception e) {
            log.error("Skipping Kafka notification due to connection error: {}", e.getMessage());
        }
    }
}
