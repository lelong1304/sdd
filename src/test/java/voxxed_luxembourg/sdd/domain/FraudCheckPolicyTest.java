package voxxed_luxembourg.sdd.domain;

import org.junit.jupiter.api.Test;
import voxxed_luxembourg.sdd.fraudcheck.domain.*;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class FraudCheckPolicyTest {

    private final FraudCheckPolicy policy = new FraudCheckPolicy(Map.of(
            "EUR", Money.of("1000", "EUR"),
            "USD", Money.of("1000", "USD")
    ));

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

    private static OrderPlaced orderOf(String amount, String currency,
                                        String accountCountry, String shippingCountry) {
        return new OrderPlaced(
                "order-1",
                "account-1",
                Money.of(amount, currency),
                accountCountry,
                shippingCountry
        );
    }
}
