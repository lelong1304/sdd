# Tasks 001 — Fraud Check (breakdown)

> Ordered, testable units. TDD: test first, then code.

## Domain

- [ ] **T1 — `Money` value object**
  Record (`BigDecimal`, currency). `Money.of(amount, currency)`,
  `isGreaterThan(Money)` rejects mixed-currency comparison.
  *Tests*: 1000.01 EUR > 1000 EUR, 1000 EUR not > 1000 EUR, cross-currency throws.

- [ ] **T2 — Domain events & enum**
  Records `OrderPlaced`, `FraudCheckRequested`. Enum `FraudReason`.
  *Test*: null required fields rejected.

- [ ] **T3 — `FraudCheckPolicy` (Rules 1 & 2)**
  `evaluate(OrderPlaced)` returns `Optional<FraudCheckRequested>`.
  Threshold per currency, default 1000.
  *Tests*:
  - `publishesFraudCheck_whenAmountStrictlyAboveThreshold`
  - `noFraudCheck_whenAmountEqualsThreshold`
  - `appliesThresholdPerCurrency`
  - `crossBorderTrue_whenShippingCountryDiffers`
  - `crossBorderFalse_whenShippingCountryMatches`

## Application

- [ ] **T4 — `FraudCheckUseCase`**
  Calls policy; if result present, calls publisher port.
  *Tests*: `callsPublisher_whenPolicyMatches`, `doesNotCallPublisher_whenNoMatch`.

## Infrastructure

- [ ] **T5 — Kafka wiring**
  DTOs + manual mappers, `OrderPlacedListener` (no logic), `FraudCheckPublisher`,
  `KafkaConfig` (topics, JSON, consumer group).

- [ ] **T6 — Integration smoke test (Testcontainers)**
  Publish `OrderPlaced` > threshold cross-border → assert outbound event.

## Definition of done

- All unit tests green, no broker or Spring context needed.
- Every acceptance criterion in `spec.md` covered by a named test.
- No framework import in the `domain/` package.
