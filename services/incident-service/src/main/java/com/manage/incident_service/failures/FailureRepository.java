package com.manage.incident_service.failures;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface FailureRepository extends JpaRepository<FailureEvents,Long> {
    int countAllByFailureTypeAndOccurredAtBetweenAndSourceService(FailureType failureType, Instant occurredAt, Instant occurredAt2,String service);

    boolean existsByCorrelationId(String s);
}