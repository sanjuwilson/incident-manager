package com.manage.incident_service.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.internals.Topic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
@Configuration
public class KafkaConfiguration {
    @Bean
    public NewTopic createTopic(){
        return TopicBuilder.name("msg_topic").build();
    }
    @Bean
    public NewTopic createAnotherTopic(){
        return TopicBuilder.name("notification_topic").build();
    }
}
