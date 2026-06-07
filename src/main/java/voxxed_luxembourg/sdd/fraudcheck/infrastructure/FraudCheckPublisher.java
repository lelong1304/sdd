package voxxed_luxembourg.sdd.fraudcheck.infrastructure;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import voxxed_luxembourg.sdd.fraudcheck.domain.FraudCheckPublisherPort;
import voxxed_luxembourg.sdd.fraudcheck.domain.FraudCheckRequested;

@Component
public class FraudCheckPublisher implements FraudCheckPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public FraudCheckPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.fraud-checks-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(FraudCheckRequested event) {
        FraudCheckRequestedDto dto = EventMapper.toDto(event);
        kafkaTemplate.send(topic, event.orderId(), dto);
    }
}
