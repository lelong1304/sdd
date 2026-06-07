package voxxed_luxembourg.sdd.fraudcheck.infrastructure;

import java.math.BigDecimal;

/**
 * Wire DTO for outbound Kafka messages.
 */
public record FraudCheckRequestedDto(
        String orderId,
        String accountId,
        BigDecimal amount,
        String currency,
        boolean crossBorder,
        String reason
) {}
