package voxxed_luxembourg.sdd.fraudcheck.domain;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Core domain service. Pure function — given an OrderPlaced, decide whether
 * a FraudCheckRequested should be emitted, and build it.
 *
 * Holds a per-currency threshold table. Currencies without a configured
 * threshold are treated as never requiring a check (fail-open is acceptable
 * here since downstream fraud review is the safety net).
 */
public final class FraudCheckPolicy {

    private final Map<String, Money> thresholdsByCurrency;

    public FraudCheckPolicy(Map<String, Money> thresholdsByCurrency) {
        Objects.requireNonNull(thresholdsByCurrency, "thresholds must not be null");
        this.thresholdsByCurrency = Map.copyOf(thresholdsByCurrency);
    }

    public Optional<FraudCheckRequested> evaluate(OrderPlaced order) {
        Objects.requireNonNull(order, "order must not be null");

        // Rule 1 — amount strictly greater than the per-currency threshold
        if (!matchesAmountRule(order)) {
            return Optional.empty();
        }

        // Rule 2 — cross-border flag (only computed when a check is triggered)
        boolean crossBorder = !order.accountCountry().equals(order.shippingCountry());

        return Optional.of(new FraudCheckRequested(
                order.orderId(),
                order.accountId(),
                order.amount(),
                crossBorder,
                FraudReason.AMOUNT_ABOVE_THRESHOLD
        ));
    }

    private boolean matchesAmountRule(OrderPlaced order) {
        Money threshold = thresholdsByCurrency.get(order.amount().currency());
        if (threshold == null) {
            return false;
        }
        return order.amount().isGreaterThan(threshold);
    }
}
