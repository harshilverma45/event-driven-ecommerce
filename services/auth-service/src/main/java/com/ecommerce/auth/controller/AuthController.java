package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.event.UserAuthenticatedEvent;
import com.ecommerce.auth.producer.AuthEventProducer;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.security.JwtUtil;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthEventProducer authEventProducer;

    public AuthController(
            UserRepository userRepository,
            JwtUtil jwtUtil,
            AuthEventProducer authEventProducer
    ) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authEventProducer = authEventProducer;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElse(null);

        if (user == null || !user.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid email or password"));
        }

        String token = jwtUtil.generateToken(user.getEmail());
        try {
            authEventProducer.publishUserAuthenticated(
                new UserAuthenticatedEvent(
                    UUID.randomUUID().toString(),
                    user.getId(),
                    user.getName() != null ? user.getName() : user.getEmail(),
                    Instant.now()
                )
            );
        } catch (RuntimeException ex) {
            log.warn("Failed to publish USER_AUTHENTICATED event for email={}", user.getEmail(), ex);
        }

        return ResponseEntity.ok(Map.of(
            "token", token,
            "email", user.getEmail(),
            "role", user.getRole()
        ));
    }
}
