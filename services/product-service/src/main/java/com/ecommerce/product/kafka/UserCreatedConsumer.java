package com.ecommerce.product.kafka;

import com.ecommerce.product.event.UserCreatedEvent;
import com.ecommerce.product.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatedConsumer {

    private final CartService cartService;

    @KafkaListener(topics = "user-created-events", groupId = "product-service-group")
    public void consume(UserCreatedEvent event) {
        log.info("Received UserCreatedEvent for userId={}", event.getUserId());
        cartService.createCartForUser(event.getUserId());
    }
}