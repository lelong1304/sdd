package voxxed_luxembourg.sdd.application;

import org.junit.jupiter.api.Test;
import voxxed_luxembourg.sdd.fraudcheck.application.FraudCheckUseCase;
import voxxed_luxembourg.sdd.fraudcheck.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FraudCheckUseCaseTest {

    private final FraudCheckPolicy policy = new FraudCheckPolicy(Map.of(
            "EUR", Money.of("1000", "EUR")
    ));
    private final RecordingPublisher publisher = new RecordingPublisher();
    private final FraudCheckUseCase useCase = new FraudCheckUseCase(policy, publisher);

    @Test
    void callsPublisher_whenPolicyMatches() {
        OrderPlaced order = orderOf("1500", "EUR", "FR", "DE");
        useCase.handle(order);
        assertThat(publisher.published).hasSize(1);
    }

    @Test
    void doesNotCallPublisher_whenNoRuleMatches() {
        OrderPlaced order = orderOf("500", "EUR", "FR", "FR");
        useCase.handle(order);
        assertThat(publisher.published).isEmpty();
    }

    private static OrderPlaced orderOf(String amount, String currency,
                                        String accountCountry, String shippingCountry) {
        return new OrderPlaced("order-1", "account-1",
                Money.of(amount, currency), accountCountry, shippingCountry);
    }

    /** In-domain test double — per constitution: no mocking library on the domain. */
    private static final class RecordingPublisher implements FraudCheckPublisherPort {
        final List<FraudCheckRequested> published = new ArrayList<>();
        @Override
        public void publish(FraudCheckRequested event) {
            published.add(event);
        }
    }
}
