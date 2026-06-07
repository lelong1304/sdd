package voxxed_luxembourg.sdd.fraudcheck.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Money value object — amount + currency.
 * Per constitution §2: never a double, always BigDecimal. No null fields.
 */
public record Money(BigDecimal amount, String currency) {

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
    }

    public static Money of(String amount, String currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    /**
     * Strictly greater-than comparison, only valid between same-currency amounts.
     * Cross-currency comparison throws — the domain refuses to convert.
     */
    public boolean isGreaterThan(Money other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "cannot compare amounts in different currencies: %s vs %s"
                    .formatted(this.currency, other.currency));
        }
        return this.amount.compareTo(other.amount) > 0;
    }
}
