package com.manage.incident_service.kafka.consumer;


import com.manage.incident_service.failures.FailureRecord;
import com.manage.incident_service.failures.FailureService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsumeFailure {
    private final FailureService failureService;
    @KafkaListener(topics = "msg_topic",groupId = "group1")
    public void receiveFailure(FailureRecord failureRecord){
        failureService.save(failureRecord);
    }
}
