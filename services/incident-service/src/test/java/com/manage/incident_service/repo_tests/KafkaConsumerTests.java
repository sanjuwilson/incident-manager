package com.manage.incident_service.repo_tests;

import com.manage.incident_service.failures.FailureRecord;
import com.manage.incident_service.failures.FailureService;
import com.manage.incident_service.failures.FailureType;
import com.manage.incident_service.kafka.consumer.ConsumeFailure;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SpringJUnitConfig(classes = KafkaConsumerTests.KafkaOnlyConfig.class)
@EmbeddedKafka(
        partitions = 1,
        topics = "msg_topic"
)
class KafkaConsumerTests {

    @Autowired
    private KafkaTemplate<String, FailureRecord> kafkaTemplate;

    @Autowired
    private FailureService failureService;

    @Test
    void whenFailureRecordPublished_shouldCallFailureService() throws Exception {
        FailureRecord record = new FailureRecord(
                "order-service",
                FailureType.TIMEOUT,
                "POST /place-order",
                "product-service",
                Instant.now(),
                "corr-kafka-1"
        );

        kafkaTemplate.send("msg_topic", "corr-kafka-1", record)
                .get(5, TimeUnit.SECONDS);

        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(failureService).save(argThat(received ->
                                received.sourceService().equals("order-service") &&
                                        received.failureType() == FailureType.TIMEOUT &&
                                        received.operation().equals("POST /place-order") &&
                                        received.dependency().equals("product-service") &&
                                        received.correlationId().equals("corr-kafka-1")
                        ))
                );
    }

    @Configuration
    @EnableKafka
    static class KafkaOnlyConfig {

        @Bean
        FailureService failureService() {
            return mock(FailureService.class);
        }

        @Bean
        ConsumeFailure failureConsumer(FailureService failureService) {
            return new ConsumeFailure(failureService);
        }

        @Bean
        ProducerFactory<String, FailureRecord> producerFactory(
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
        KafkaTemplate<String, FailureRecord> kafkaTemplate(
                ProducerFactory<String, FailureRecord> producerFactory
        ) {
            return new KafkaTemplate<>(producerFactory);
        }

        @Bean
        ConsumerFactory<String, FailureRecord> consumerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
        ) {
            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "failure-consumer-test-group");
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            JacksonJsonDeserializer<FailureRecord> valueDeserializer =
                    new JacksonJsonDeserializer<>(FailureRecord.class);

            valueDeserializer.addTrustedPackages("*");

            return new DefaultKafkaConsumerFactory<>(
                    props,
                    new StringDeserializer(),
                    valueDeserializer
            );
        }

        @Bean
        ConcurrentKafkaListenerContainerFactory<String, FailureRecord>
        kafkaListenerContainerFactory(
                ConsumerFactory<String, FailureRecord> consumerFactory
        ) {
            ConcurrentKafkaListenerContainerFactory<String, FailureRecord> factory =
                    new ConcurrentKafkaListenerContainerFactory<>();

            factory.setConsumerFactory(consumerFactory);
            return factory;
        }
    }
}