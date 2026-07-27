package com.ecommerce.product.service;

import com.ecommerce.product.entity.Cart;
import com.ecommerce.product.repository.CartRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;

    /*
     * Testing instructions:
     * 1) Re-run consumer from beginning: use kafka-console-consumer with --from-beginning.
     * 2) Publish/trigger the same user-created flow multiple times for the same userId.
     * Expected:
     * - Exactly one cart row per userId.
     * - Logs show "already exists" or "DB level duplicate prevented" for duplicates.
     */
    public void createCartForUser(String userId) {
        log.info("Received cart creation request for user: {}", userId);

        Optional<Cart> existingCart = cartRepository.findByUserId(userId);
        if (existingCart.isPresent()) {
            log.info("Cart already exists for user: {}", userId);
            return;
        }

        try {
            Cart cart = Cart.builder()
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .build();

            cartRepository.save(cart);
            log.info("Cart created for user: {}", userId);
        } catch (DataIntegrityViolationException exception) {
            log.warn("Duplicate cart prevented at DB level for user: {}", userId);
        }
    }
}
