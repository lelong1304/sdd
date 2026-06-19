package voxxed_luxembourg.sdd.fraudcheck.domain;

import java.util.Objects;

/**
 * Domain representation of the outbound FraudCheckRequested event.
 */
public record FraudCheckRequested(
        String orderId,
        String accountId,
        Money amount,
        boolean crossBorder,
        FraudReason reason
) {
    public FraudCheckRequested {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
    }
}
