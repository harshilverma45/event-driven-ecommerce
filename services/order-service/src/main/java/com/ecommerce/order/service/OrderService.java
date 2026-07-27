package com.ecommerce.order.service;

import com.ecommerce.order.dto.ProductResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.event.OrderCreatedEvent;
import com.ecommerce.order.producer.OrderEventProducer;
import com.ecommerce.order.repository.OrderRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;
    private final WebClient.Builder webClientBuilder;

    /*
     * Startup order:
     * 1) Start discovery-server.
     * 2) Start auth-service, product-service, order-service, and api-gateway.
     *
     * Verification:
     * - Open http://localhost:8761 and confirm services are registered.
     * - Create an order and verify Order Service calls Product Service by service name.
     */
    public Order createOrder(Order order) {
        try {
            String productServiceUri = "http://product-service/products/" + order.getProductId();
            log.info("Calling Product Service via discovery: uri={}", productServiceUri);

            ProductResponse product = webClientBuilder.build()
                    .get()
                    .uri(productServiceUri)
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .block();

            if (product == null) {
                log.warn("Validation failed: product not found for productId={}", order.getProductId());
                throw new RuntimeException("Product not found");
            }

            log.info("Product fetch successful via service name: productId={}, price={}, stock={}",
                    product.getId(), product.getPrice(), product.getQuantity());

            if (product.getQuantity() < order.getQuantity()) {
                log.warn("Validation failed: insufficient stock for productId={}, requested={}, available={}",
                        order.getProductId(), order.getQuantity(), product.getQuantity());
                throw new RuntimeException("Insufficient stock");
            }

            double totalPrice = product.getPrice() * order.getQuantity();
            order.setTotalPrice(totalPrice);
            order.setStatus(OrderStatus.CREATED);
            order.setCreatedAt(LocalDateTime.now());

            Order savedOrder = orderRepository.save(order);
            log.info("Order created successfully: orderId={}, productId={}, quantity={}, totalPrice={}",
                    savedOrder.getId(), savedOrder.getProductId(), savedOrder.getQuantity(), savedOrder.getTotalPrice());

            OrderCreatedEvent event = OrderCreatedEvent.builder()
                    .orderId(savedOrder.getId())
                    .userEmail(savedOrder.getUserEmail())
                    .productId(savedOrder.getProductId())
                    .quantity(savedOrder.getQuantity())
                    .totalPrice(savedOrder.getTotalPrice())
                    .build();

            orderEventProducer.sendOrderCreatedEvent(event);
            log.info("Order event published: orderId={}", savedOrder.getId());

            return savedOrder;
        } catch (WebClientResponseException ex) {
            log.error("Product Service responded with error for productId={}: status={}, body={}",
                    order.getProductId(), ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw new RuntimeException("Failed to validate product from Product Service", ex);
        } catch (WebClientRequestException ex) {
            log.error("Failed service-to-service communication with Product Service for productId={}", order.getProductId(), ex);
            throw new RuntimeException("Product Service is unavailable", ex);
        } catch (RuntimeException ex) {
            log.error("Order creation failed for productId={} and userEmail={}", order.getProductId(), order.getUserEmail(), ex);
            throw ex;
        }
    }
}
