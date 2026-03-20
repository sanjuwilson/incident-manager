package com.manage.notification.notification_service.consumer;


import com.manage.email.EmailService;
import com.manage.incident_service.incidents.IncidentNotification;
import com.manage.incident_service.incidents.IncidentStatus;
import com.manage.notification.notification_service.notification.Notification;
import com.manage.notification.notification_service.notification.NotificationService;
import com.manage.notification.notification_service.notification.SendNotification;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {
    private final EmailService emailService;
    private final NotificationService notificationService;

    @KafkaListener(topics = "notification_topic",groupId = "group1")
    public void receiveOrderConfirmation(IncidentNotification incidentNotification) throws MessagingException {
        System.out.println("Consumer Notification "+incidentNotification);
        Notification notification = notificationService.save(incidentNotification);

        emailService.sendNotification(notification.getId());



    }


}

