package voxxed_luxembourg.sdd.fraudcheck.infrastructure;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import voxxed_luxembourg.sdd.fraudcheck.application.FraudCheckUseCase;

/**
 * Kafka adapter — consumes OrderPlaced events.
 * No business logic here (constitution §4): maps and delegates.
 */
@Component
public class OrderPlacedListener {

    private final FraudCheckUseCase useCase;

    public OrderPlacedListener(FraudCheckUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = "${app.kafka.orders-topic}", groupId = "${app.kafka.consumer-group}")
    public void onOrderPlaced(OrderPlacedDto dto) {
        useCase.handle(EventMapper.toDomain(dto));
    }
}
