package com.ecommerce.gateway.security;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of("/auth/", "/actuator/");
    private static final List<String> PROTECTED_PATH_PREFIXES = List.of("/products/", "/orders/", "/notifications/");

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.info("Incoming gateway request: path={}", path);

        if (isPublicPath(path) || !isProtectedPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Unauthorized access attempt: missing/invalid Authorization header for path={}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            log.warn("Token validation failed for path={}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String userEmail = jwtUtil.extractUsername(token);
        String userId = jwtUtil.extractUserId(token);

        ServerWebExchange updatedExchange = exchange.mutate().request(request -> {
            if (userEmail != null && !userEmail.isBlank()) {
                request.header("X-User-Email", userEmail);
            }
            if (userId != null && !userId.isBlank()) {
                request.header("X-User-Id", userId);
            }
        }).build();

        log.info("Token validation successful for path={} userEmail={}", path, userEmail);
        return chain.filter(updatedExchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private boolean isProtectedPath(String path) {
        return PROTECTED_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }
}
