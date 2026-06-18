package voxxed_luxembourg.sdd.fraudcheck.domain;

import java.time.Duration;
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

    private static final Duration RECENT_ACCOUNT_WINDOW = Duration.ofHours(24);
    private static final String RECENT_ACCOUNT_THRESHOLD = "100";

    private final Map<String, Money> thresholdsByCurrency;

    public FraudCheckPolicy(Map<String, Money> thresholdsByCurrency) {
        Objects.requireNonNull(thresholdsByCurrency, "thresholds must not be null");
        this.thresholdsByCurrency = Map.copyOf(thresholdsByCurrency);
    }

    public Optional<FraudCheckRequested> evaluate(OrderPlaced order) {
        Objects.requireNonNull(order, "order must not be null");

        boolean rule1 = matchesAmountRule(order);
        boolean rule3 = matchesRecentAccountRule(order);

        if (!rule1 && !rule3) {
            return Optional.empty();
        }

        // Rule 2 — cross-border flag, always applied when a check is triggered
        boolean crossBorder = !order.accountCountry().equals(order.shippingCountry());

        // Rule 1 takes precedence when both rules match (higher-confidence signal)
        FraudReason reason = rule1
                ? FraudReason.AMOUNT_ABOVE_THRESHOLD
                : FraudReason.RECENT_ACCOUNT_LOW_AMOUNT;

        return Optional.of(new FraudCheckRequested(
                order.orderId(),
                order.accountId(),
                order.amount(),
                crossBorder,
                reason
        ));
    }

    // Rule 1 — amount strictly greater than the per-currency threshold
    private boolean matchesAmountRule(OrderPlaced order) {
        Money threshold = thresholdsByCurrency.get(order.amount().currency());
        if (threshold == null) {
            return false;
        }
        return order.amount().isGreaterThan(threshold);
    }

    // Rule 3 — account created strictly less than 24h before placedAt,
    //           and amount strictly greater than 100 in the order's currency
    private boolean matchesRecentAccountRule(OrderPlaced order) {
        Duration accountAge = Duration.between(order.accountCreatedAt(), order.placedAt());
        if (accountAge.compareTo(RECENT_ACCOUNT_WINDOW) >= 0) {
            return false;
        }
        Money recentThreshold = Money.of(RECENT_ACCOUNT_THRESHOLD, order.amount().currency());
        return order.amount().isGreaterThan(recentThreshold);
    }
}

