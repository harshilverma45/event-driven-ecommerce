package com.ecommerce.product.kafka;

import com.ecommerce.product.event.UserLoggedInEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProductEventConsumer {

    @KafkaListener(topics = "user-login-events", groupId = "product-group")
    public void consume(UserLoggedInEvent event) {
        System.out.println("Product Service received event:");
        System.out.println("User logged in: " + event.getEmail());
    }
}
