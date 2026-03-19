package com.ecommerce.auth.event;

import java.time.Instant;

public interface DomainEvent {

    String eventType();

    Instant occurredAt();
}
