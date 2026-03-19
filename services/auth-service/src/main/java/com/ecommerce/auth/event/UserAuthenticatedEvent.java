package com.ecommerce.auth.event;

import java.time.Instant;

public record UserAuthenticatedEvent(
        String eventId,
        Long userId,
        String username,
        Instant occurredAt
) implements DomainEvent {

    @Override
    public String eventType() {
        return "USER_AUTHENTICATED";
    }
}
