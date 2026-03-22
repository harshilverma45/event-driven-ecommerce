package com.ecommerce.auth.producer;

import com.ecommerce.auth.event.UserAuthenticatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthEventProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.kafka.topics.user-authenticated}")
    private String userAuthenticatedTopic;

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
