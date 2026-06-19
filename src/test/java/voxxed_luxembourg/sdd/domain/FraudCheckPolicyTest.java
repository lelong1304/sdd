package voxxed_luxembourg.sdd.domain;

import org.junit.jupiter.api.Test;
import voxxed_luxembourg.sdd.fraudcheck.domain.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class FraudCheckPolicyTest {

    private final FraudCheckPolicy policy = new FraudCheckPolicy(Map.of(
            "EUR", Money.of("1000", "EUR"),
            "USD", Money.of("1000", "USD")
    ));

    // Fixed reference time — keeps tests deterministic
    private static final Instant PLACED_AT      = Instant.parse("2026-06-18T12:00:00Z");
    private static final Instant OLD_ACCOUNT    = PLACED_AT.minusSeconds(48 * 3600); // 48h — never triggers Rule 3
    private static final Instant RECENT_ACCOUNT = PLACED_AT.minusSeconds(12 * 3600); // 12h — triggers Rule 3

    // --- Rule 1 — amount threshold ---

    @Test
    void publishesFraudCheck_whenAmountStrictlyAboveThreshold() {
        OrderPlaced order = orderOf("1000.01", "EUR", "FR", "FR");
        Optional<FraudCheckRequested> result = policy.evaluate(order);

        assertThat(result).isPresent();
        assertThat(result.get().reason()).isEqualTo(FraudReason.AMOUNT_ABOVE_THRESHOLD);
    }

    @Test
    void noFraudCheck_whenAmountEqualsThreshold() {
        OrderPlaced order = orderOf("1000", "EUR", "FR", "FR");
        assertThat(policy.evaluate(order)).isEmpty();
    }

    @Test
    void noFraudCheck_whenAmountBelowThreshold() {
        OrderPlaced order = orderOf("500", "EUR", "FR", "FR");
        assertThat(policy.evaluate(order)).isEmpty();
    }

    @Test
    void appliesThresholdPerCurrency() {
        OrderPlaced order = orderOf("1500", "USD", "US", "US");
        assertThat(policy.evaluate(order))
                .isPresent()
                .get()
                .extracting(FraudCheckRequested::reason)
                .isEqualTo(FraudReason.AMOUNT_ABOVE_THRESHOLD);
    }

    // --- Rule 2 — cross-border ---

    @Test
    void crossBorderTrue_whenShippingCountryDiffers() {
        OrderPlaced order = orderOf("1500", "EUR", "FR", "DE");
        assertThat(policy.evaluate(order))
                .isPresent()
                .get()
                .extracting(FraudCheckRequested::crossBorder)
                .isEqualTo(true);
    }

    @Test
    void crossBorderFalse_whenShippingCountryMatches() {
        OrderPlaced order = orderOf("1500", "EUR", "FR", "FR");
        assertThat(policy.evaluate(order))
                .isPresent()
                .get()
                .extracting(FraudCheckRequested::crossBorder)
                .isEqualTo(false);
    }

    // --- Rule 3 — recent account ---

    @Test
    void publishesFraudCheck_whenRecentAccountAndAmountAbove100() {
        OrderPlaced order = recentAccountOrderOf("101", "EUR", "FR", "FR");
        assertThat(policy.evaluate(order))
                .isPresent()
                .get()
                .extracting(FraudCheckRequested::reason)
                .isEqualTo(FraudReason.RECENT_ACCOUNT_LOW_AMOUNT);
    }

    @Test
    void noFraudCheck_whenRecentAccountButAmountExactly100() {
        // Boundary: exactly 100 is NOT strictly greater — no trigger
        OrderPlaced order = recentAccountOrderOf("100", "EUR", "FR", "FR");
        assertThat(policy.evaluate(order)).isEmpty();
    }

    @Test
    void noFraudCheck_whenRecentAccountButAmountBelow100() {
        OrderPlaced order = recentAccountOrderOf("50", "EUR", "FR", "FR");
        assertThat(policy.evaluate(order)).isEmpty();
    }

    @Test
    void noFraudCheck_whenAccountAgeExactly24Hours() {
        // Boundary: exactly 24h is NOT strictly less than 24h — no trigger
        Instant exactly24hBefore = PLACED_AT.minusSeconds(24 * 3600);
        OrderPlaced order = orderWithInstants("101", "EUR", "FR", "FR", exactly24hBefore, PLACED_AT);
        assertThat(policy.evaluate(order)).isEmpty();
    }

    @Test
    void noFraudCheck_whenAccountOlderThan24Hours() {
        // OLD_ACCOUNT is 48h old — Rule 3 must not fire
        OrderPlaced order = orderOf("101", "EUR", "FR", "FR");
        assertThat(policy.evaluate(order)).isEmpty();
    }

    @Test
    void rule3_crossBorderApplied_whenShippingCountryDiffers() {
        OrderPlaced order = recentAccountOrderOf("101", "EUR", "FR", "DE");
        assertThat(policy.evaluate(order))
                .isPresent()
                .get()
                .extracting(FraudCheckRequested::crossBorder)
                .isEqualTo(true);
    }

    // --- Rule precedence — Rule 1 wins when both match ---

    @Test
    void rule1WinsPrecedence_whenBothRule1AndRule3Match() {
        // Amount > 1000 (Rule 1) AND recent account with amount > 100 (Rule 3) — Rule 1 wins
        OrderPlaced order = recentAccountOrderOf("1500", "EUR", "FR", "FR");
        assertThat(policy.evaluate(order))
                .isPresent()
                .get()
                .extracting(FraudCheckRequested::reason)
                .isEqualTo(FraudReason.AMOUNT_ABOVE_THRESHOLD);
    }

    // --- output content ---

    @Test
    void preservesOrderAndAccountIds_inOutboundEvent() {
        OrderPlaced order = orderOf("1500", "EUR", "FR", "FR");
        FraudCheckRequested event = policy.evaluate(order).orElseThrow();

        assertThat(event.orderId()).isEqualTo(order.orderId());
        assertThat(event.accountId()).isEqualTo(order.accountId());
        assertThat(event.amount()).isEqualTo(order.amount());
    }

    // --- helpers ---

    /** Old account (48h) — Rule 3 never fires, safe default for Rule 1/2 tests. */
    private static OrderPlaced orderOf(String amount, String currency,
                                        String accountCountry, String shippingCountry) {
        return orderWithInstants(amount, currency, accountCountry, shippingCountry,
                OLD_ACCOUNT, PLACED_AT);
    }

    /** Recent account (12h) — Rule 3 can fire when amount > 100. */
    private static OrderPlaced recentAccountOrderOf(String amount, String currency,
                                                     String accountCountry, String shippingCountry) {
        return orderWithInstants(amount, currency, accountCountry, shippingCountry,
                RECENT_ACCOUNT, PLACED_AT);
    }

    private static OrderPlaced orderWithInstants(String amount, String currency,
                                                  String accountCountry, String shippingCountry,
                                                  Instant accountCreatedAt, Instant placedAt) {
        return new OrderPlaced(
                "order-1", "account-1",
                Money.of(amount, currency),
                accountCountry, shippingCountry,
                accountCreatedAt, placedAt
        );
    }
}


