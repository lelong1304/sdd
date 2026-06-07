package voxxed_luxembourg.sdd.domain;

import org.junit.jupiter.api.Test;
import voxxed_luxembourg.sdd.fraudcheck.domain.Money;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void isGreaterThan_trueWhenAmountStrictlyAbove() {
        Money a = Money.of("1000.01", "EUR");
        Money b = Money.of("1000", "EUR");
        assertThat(a.isGreaterThan(b)).isTrue();
    }

    @Test
    void isGreaterThan_falseWhenAmountEquals() {
        Money a = Money.of("1000", "EUR");
        Money b = Money.of("1000", "EUR");
        assertThat(a.isGreaterThan(b)).isFalse();
    }

    @Test
    void isGreaterThan_falseWhenAmountBelow() {
        Money a = Money.of("999.99", "EUR");
        Money b = Money.of("1000", "EUR");
        assertThat(a.isGreaterThan(b)).isFalse();
    }

    @Test
    void isGreaterThan_rejectsCrossCurrencyComparison() {
        Money eur = Money.of("1000", "EUR");
        Money usd = Money.of("1000", "USD");
        assertThatThrownBy(() -> eur.isGreaterThan(usd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("different currencies");
    }

    @Test
    void constructor_rejectsNegativeAmount() {
        assertThatThrownBy(() -> Money.of("-1", "EUR"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
