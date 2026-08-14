package com.manage.incident_service.repo_tests;


import com.manage.incident_service.failures.FailureType;
import com.manage.incident_service.incidents.IncidentNotification;
import com.manage.incident_service.incidents.IncidentStatus;
import com.manage.incident_service.incidents.Severity;
import com.manage.incident_service.kafka.producer.ProduceNotification;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = KafkaProducerTests.KafkaProducerTestConfig.class)
@EmbeddedKafka(
        partitions = 1,
        topics = "notification_topic",
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class KafkaProducerTests {

    @Autowired
    private ProduceNotification produceNotification;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Test
    void sendIncidentNotification_shouldPublishNotificationToKafka() {
        UUID incidentId = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();

        IncidentNotification notification = new IncidentNotification(
                incidentId,
                "TIMEOUT in order-service",
                "order-service",
                FailureType.TIMEOUT,
                Severity.HIGH,
                IncidentStatus.OPEN,
                Instant.now(),
                ruleId
        );

        Consumer<String, IncidentNotification> consumer = createConsumer();
        consumer.subscribe(List.of("notification_topic"));

        produceNotification.sendIncidentNotification(notification);

        ConsumerRecord<String, IncidentNotification> record =
                KafkaTestUtils.getSingleRecord(
                        consumer,
                        "notification_topic",
                        Duration.ofSeconds(10)
                );

        IncidentNotification received = record.value();

        assertThat(record.topic()).isEqualTo("notification_topic");
        assertThat(received.id()).isEqualTo(incidentId);
        assertThat(received.title()).isEqualTo("TIMEOUT in order-service");
        assertThat(received.sourceService()).isEqualTo("order-service");
        assertThat(received.failureType()).isEqualTo(FailureType.TIMEOUT);
        assertThat(received.severity()).isEqualTo(Severity.HIGH);
        assertThat(received.status()).isEqualTo(IncidentStatus.OPEN);
        assertThat(received.ruleId()).isEqualTo(ruleId);

        consumer.close();
    }

    private Consumer<String, IncidentNotification> createConsumer() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-producer-test-group-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JacksonJsonDeserializer<IncidentNotification> valueDeserializer =
                new JacksonJsonDeserializer<>(IncidentNotification.class);

        valueDeserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                valueDeserializer
        ).createConsumer();
    }

    @Configuration
    static class KafkaProducerTestConfig {

        @Bean
        ProduceNotification produceNotification(
                KafkaTemplate<String, IncidentNotification> kafkaTemplate
        ) {
            return new ProduceNotification(kafkaTemplate);
        }

        @Bean
        ProducerFactory<String, IncidentNotification> producerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
        ) {
            Map<String, Object> props = new HashMap<>();

            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

            return new DefaultKafkaProducerFactory<>(
                    props,
                    new StringSerializer(),
                    new JacksonJsonSerializer<>()
            );
        }

        @Bean
        KafkaTemplate<String, IncidentNotification> kafkaTemplate(
                ProducerFactory<String, IncidentNotification> producerFactory
        ) {
            return new KafkaTemplate<>(producerFactory);
        }
    }
}