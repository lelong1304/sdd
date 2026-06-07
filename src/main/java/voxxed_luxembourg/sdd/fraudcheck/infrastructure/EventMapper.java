package voxxed_luxembourg.sdd.fraudcheck.infrastructure;


import voxxed_luxembourg.sdd.fraudcheck.domain.FraudCheckRequested;
import voxxed_luxembourg.sdd.fraudcheck.domain.Money;
import voxxed_luxembourg.sdd.fraudcheck.domain.OrderPlaced;

/**
 * Manual mapping between wire DTOs and domain records.
 * Per constitution: no mapping library, the boundary stays explicit.
 */
final class EventMapper {

    private EventMapper() {}

    static OrderPlaced toDomain(OrderPlacedDto dto) {
        return new OrderPlaced(
                dto.orderId(),
                dto.accountId(),
                new Money(dto.amount(), dto.currency()),
                dto.accountCountry(),
                dto.shippingCountry()
        );
    }

    static FraudCheckRequestedDto toDto(FraudCheckRequested event) {
        return new FraudCheckRequestedDto(
                event.orderId(),
                event.accountId(),
                event.amount().amount(),
                event.amount().currency(),
                event.crossBorder(),
                event.reason().name()
        );
    }
}
