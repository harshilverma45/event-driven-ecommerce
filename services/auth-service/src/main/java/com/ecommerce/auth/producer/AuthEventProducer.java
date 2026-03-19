package com.ecommerce.auth.producer;

import com.ecommerce.auth.event.UserAuthenticatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String userAuthenticatedTopic;

    public AuthEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.user-authenticated}") String userAuthenticatedTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.userAuthenticatedTopic = userAuthenticatedTopic;
    }

    public void publishUserAuthenticated(UserAuthenticatedEvent event) {
        String key = event.userId() != null ? String.valueOf(event.userId()) : event.username();
        kafkaTemplate.send(
                userAuthenticatedTopic,
                key,
                toJson(event)
        );
    }

    private String toJson(UserAuthenticatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize UserAuthenticatedEvent", ex);
        }
    }
}
