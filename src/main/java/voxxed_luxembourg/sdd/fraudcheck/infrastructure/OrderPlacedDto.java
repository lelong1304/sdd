package voxxed_luxembourg.sdd.fraudcheck.infrastructure;

import java.math.BigDecimal;

/**
 * Wire DTO for inbound Kafka messages. Anti-corruption boundary —
 * never imported by the domain.
 */
public record OrderPlacedDto(
        String orderId,
        String accountId,
        BigDecimal amount,
        String currency,
        String accountCountry,
        String shippingCountry
) {}
