package com.manage.incident_service.kafka.producer;

import com.manage.incident_service.incidents.IncidentNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProduceNotification {
    private final KafkaTemplate<String, IncidentNotification> kafkaTemplate;

    public void sendIncidentNotification(IncidentNotification incidentNotification) {
        Message<IncidentNotification> message= MessageBuilder
                .withPayload(incidentNotification)
                .setHeader(KafkaHeaders.TOPIC,"notification_topic")
                .build();
        kafkaTemplate.send(message);
        System.out.println("sendPointsMessage");



    }


}
