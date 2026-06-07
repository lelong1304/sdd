package voxxed_luxembourg.sdd.fraudcheck.infrastructure;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import voxxed_luxembourg.sdd.fraudcheck.application.FraudCheckUseCase;
import voxxed_luxembourg.sdd.fraudcheck.domain.FraudCheckPolicy;
import voxxed_luxembourg.sdd.fraudcheck.domain.FraudCheckPublisherPort;
import voxxed_luxembourg.sdd.fraudcheck.domain.Money;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring wiring of the framework-free domain and application beans.
 * Per-currency thresholds are declared here, not in the domain code.
 */
@Configuration
public class AppConfig {

    @Bean
    public FraudCheckPolicy fraudCheckPolicy() {
        Map<String, Money> thresholds = Map.of(
                "EUR", Money.of("1000", "EUR"),
                "USD", Money.of("1000", "USD"),
                "GBP", Money.of("1000", "GBP")
        );
        return new FraudCheckPolicy(thresholds);
    }

    @Bean
    public FraudCheckUseCase fraudCheckUseCase(
            FraudCheckPolicy policy,
            FraudCheckPublisherPort publisher) {
        return new FraudCheckUseCase(policy, publisher);
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, Object.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }


}
