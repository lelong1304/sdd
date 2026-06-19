# Plan 001 — Fraud Check (technical plan)

> Derived from `spec.md` under the rules of `constitution.md`.
> The "how". No business rules — only architecture.

## Flow

```
orders topic ──▶ Listener ──▶ UseCase ──▶ Policy (domain) ──▶ Publisher ──▶ fraud-checks topic
```

## Layering (per constitution §1)

- `domain/`
  - `Money` — record (`BigDecimal`, currency), `isGreaterThan(Money)` (same currency only).
  - `OrderPlaced`, `FraudCheckRequested` — records.
  - `FraudReason` — enum (`AMOUNT_ABOVE_THRESHOLD`).
  - `FraudCheckPolicy` — pure function `Optional<FraudCheckRequested> evaluate(OrderPlaced)`.
    Holds a `Map<String, Money>` of thresholds per currency.
  - `FraudCheckPublisherPort` — output port.
- `application/`
  - `FraudCheckUseCase` — calls policy, hands result to the publisher port.
- `infrastructure/`
  - `OrderPlacedListener` (`@KafkaListener`), `FraudCheckPublisher`, `KafkaConfig`,
    wire DTOs + mappers.

## Key decisions

- **Threshold is per currency**, configured in the domain (default 1000 in each
  known currency). `Money.isGreaterThan` rejects cross-currency comparison.
- **`Money` is BigDecimal-backed** — exact comparison, no float drift.
- **`crossBorder` is computed in the domain** when building the outbound event.
- **DTOs ≠ domain records** — mapping isolated in infrastructure.

## Testing strategy

- `FraudCheckPolicyTest` — pure unit, covers every acceptance criterion.
- `FraudCheckUseCaseTest` — mocked port, verifies call / no-call.
- Testcontainers integration test — separate suite.
