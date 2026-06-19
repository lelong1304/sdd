# Project Constitution

> Immutable engineering principles. Every spec, plan, and generated artifact
> MUST comply with this document. When a request conflicts with the constitution,
> the constitution wins — surface the conflict instead of silently working around it.

## 1. Architecture

- **Hexagonal architecture** (ports & adapters). The domain layer is the core.
- The **domain layer has ZERO framework dependencies** — no Spring, no Kafka,
  no Jackson annotations. Plain Java only.
- Infrastructure concerns (Kafka consumers/producers, serialization, config)
  live in adapter packages and depend on the domain, never the reverse.
- Package layout:
  - `domain/` — business logic, value objects, domain events, ports (interfaces)
  - `application/` — use cases orchestrating the domain
  - `infrastructure/` — Kafka adapters, Spring config, serializers

## 2. Domain modeling

- Value objects and events are **immutable** — use Java `record`.
- Money is **never** a `double`. Use a dedicated `Money` value object backed by
  `BigDecimal` + a currency. No floating-point arithmetic on amounts.
- Domain events implement a **sealed interface** so the compiler enforces
  exhaustive handling.
- No `null` in the domain. Use `Optional` at boundaries; fail fast on invariants
  in constructors.

## 3. Testing

- **Domain logic is tested without Spring and without Kafka.** Pure unit tests,
  no broker, no application context — they must run in milliseconds.
- Every business rule in a spec maps to **at least one named test** whose method
  name states the rule (e.g. `publishesFraudCheck_whenAmountAboveThreshold`).
- **No mocking of the domain.** Mocks are allowed only at infrastructure ports.
- Integration tests (real Kafka) are separate and use Testcontainers — they are
  NOT part of the fast unit suite.
- A new business rule is "done" only when a failing test is written first,
  then made green. (TDD discipline still applies — the AI writes the test too.)

## 4. Code conventions

- Java 21. Use `record`, `sealed`, pattern matching where they improve clarity.
- Constructor injection only. No field injection.
- No business logic inside Kafka listeners — listeners delegate to a use case.
- Method names and types use **ubiquitous language** from the spec
  (e.g. `OrderPlaced`, `FraudCheckRequested`, `Money`, `threshold`).

## 5. What the AI must NOT do

- Do not add dependencies not justified by a spec or this constitution.
- Do not invent business rules that aren't in the spec — ask instead.
- Do not weaken a security/fraud rule to make a test pass.
- Do not edit generated code to fix behavior — update the spec and regenerate.
