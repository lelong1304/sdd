package voxxed_luxembourg.sdd.fraudcheck.application;

import voxxed_luxembourg.sdd.fraudcheck.domain.FraudCheckPolicy;
import voxxed_luxembourg.sdd.fraudcheck.domain.FraudCheckPublisherPort;
import voxxed_luxembourg.sdd.fraudcheck.domain.OrderPlaced;

import java.util.Objects;

/**
 * Application use case — orchestrates the policy and the output port.
 * Stateless. No framework dependency (the wiring is done in infrastructure).
 */
public final class FraudCheckUseCase {

    private final FraudCheckPolicy policy;
    private final FraudCheckPublisherPort publisher;

    public FraudCheckUseCase(FraudCheckPolicy policy, FraudCheckPublisherPort publisher) {
        this.policy = Objects.requireNonNull(policy, "policy must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
    }

    public void handle(OrderPlaced order) {
        policy.evaluate(order).ifPresent(publisher::publish);
    }
}
