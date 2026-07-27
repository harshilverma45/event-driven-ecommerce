package com.ecommerce.notification.kafka;

import com.ecommerce.notification.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    /*
     * Run instructions:
     * 1) Start Kafka.
     * 2) Run notification-service: mvn spring-boot:run
     * 3) Trigger order creation from order-service.
     * 4) Verify logs for email simulation and correct event data.
     */
    @KafkaListener(
            topics = "order-created-events",
            groupId = "notification-group"
    )
    public void consume(OrderCreatedEvent event) {
        try {
            log.info("Received OrderCreatedEvent: orderId={}, userEmail={}, productId={}, quantity={}, totalPrice={}",
                    event.getOrderId(),
                    event.getUserEmail(),
                    event.getProductId(),
                    event.getQuantity(),
                    event.getTotalPrice());

            log.info("Sending email to: {}", event.getUserEmail());
            log.info("Order ID: {}", event.getOrderId());
            log.info("Total Price: {}", event.getTotalPrice());
            log.info("Email sent successfully");
        } catch (Exception ex) {
            log.error("Failed to process notification for order: {}", event.getOrderId(), ex);
        }
    }
}
