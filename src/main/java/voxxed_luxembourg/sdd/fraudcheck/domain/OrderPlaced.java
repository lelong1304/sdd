package voxxed_luxembourg.sdd.fraudcheck.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain representation of the inbound OrderPlaced event.
 * Decoupled from any Kafka/wire format (see infrastructure DTOs).
 */
public record OrderPlaced(
        String orderId,
        String accountId,
        Money amount,
        String accountCountry,
        String shippingCountry,
        Instant accountCreatedAt,
        Instant placedAt
) {
    public OrderPlaced {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(accountCountry, "accountCountry must not be null");
        Objects.requireNonNull(shippingCountry, "shippingCountry must not be null");
        Objects.requireNonNull(accountCreatedAt, "accountCreatedAt must not be null");
        Objects.requireNonNull(placedAt, "placedAt must not be null");
    }
}
