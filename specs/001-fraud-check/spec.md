# Spec 001 — Fraud Check on Order Placement

> Status: living spec. This is the source of truth.
> Code and tests are compiled outputs of this document.

## Context

We consume `OrderPlaced` events from the `orders` Kafka topic. For a subset of
orders we must trigger a downstream fraud review by publishing a
`FraudCheckRequested` event to the `fraud-checks` topic.

This service does NOT decide whether an order is fraudulent — it only decides
**which orders require a fraud review** and forwards the relevant context.

## Ubiquitous language

- **Order** — a customer purchase. Identified by an `orderId`.
- **Account** — the customer placing the order. Identified by an `accountId`,
  has a `country`.
- **Money** — an amount with a currency. Never a floating-point number.
- **Threshold** — the amount above which a fraud check is required, defined
  **per currency** (e.g. 1000 EUR, 1000 USD).
- **FraudCheckRequested** — the event asking a downstream service to review an order.

## Inbound event: `OrderPlaced`

| Field | Type | Notes |
|-------|------|-------|
| `orderId` | String | unique |
| `accountId` | String | unique |
| `amount` | Money | order total (amount + currency) |
| `accountCountry` | String | ISO country code of the account |
| `shippingCountry` | String | ISO country code of the delivery address |

## Business rules

### Rule 1 — High-amount orders require a fraud check
GIVEN an `OrderPlaced` event
WHEN the `amount` is **strictly greater than** the threshold configured
for the order's currency (default: 1000 in that currency)
THEN publish a `FraudCheckRequested` event for that order.

Amounts in different currencies are never compared or converted — each currency
has its own threshold.

### Rule 2 — Cross-border orders are flagged
GIVEN an `OrderPlaced` event that requires a fraud check (per any rule)
WHEN the `shippingCountry` differs from the `accountCountry`
THEN the published `FraudCheckRequested` event carries `crossBorder = true`.
Otherwise `crossBorder = false`.

## Outbound event: `FraudCheckRequested`

| Field | Type | Notes |
|-------|------|-------|
| `orderId` | String | from the order |
| `accountId` | String | from the order |
| `amount` | Money | from the order, same currency |
| `crossBorder` | boolean | per Rule 2 |
| `reason` | FraudReason | which rule triggered the check |

## Acceptance criteria

- An order of exactly 1000 EUR does **not** trigger a fraud check (strictly greater).
- An order of 1000.01 EUR **does** trigger a fraud check.
- An order of 1500 USD triggers a fraud check (above the USD threshold of 1000).
- An order of 500 EUR does not trigger a fraud check.
- A triggered order shipping to a different country than the account has
  `crossBorder = true`.
- A triggered order shipping to the same country has `crossBorder = false`.
- No fraud check is published for an order that matches no rule.

## Out of scope (for now)

- Deciding the fraud outcome (handled by a downstream service).
- Currency conversion between currencies.
- Persistence — this service is stateless.
