package voxxed_luxembourg.sdd.fraudcheck.domain;

/**
 * Output port — implemented by infrastructure. The domain depends on this
 * abstraction, never on the Kafka producer directly.
 */
public interface FraudCheckPublisherPort {
    void publish(FraudCheckRequested event);
}
