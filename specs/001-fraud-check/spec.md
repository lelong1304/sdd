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
| `accountCreatedAt` | Instant | when the account was created (UTC) |
| `placedAt` | Instant | when the order was placed (UTC) |

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

### Rule 3 — Recent accounts have a lower threshold
GIVEN an `OrderPlaced` event
WHEN `placedAt` minus `accountCreatedAt` is **strictly less than** 24 hours
AND the `amount` is **strictly greater than** 100 in the order's currency
THEN publish a `FraudCheckRequested` event with `reason = RECENT_ACCOUNT_LOW_AMOUNT`.

The 100 threshold follows the same per-currency convention as Rule 1 (e.g. 100 EUR,
100 USD — amounts in different currencies are never converted).

### Rule precedence — when multiple trigger rules match
Exactly **one** `FraudCheckRequested` event is published per order.
**Rule 1 takes precedence**: when both Rule 1 and Rule 3 match, `reason` is set
to `AMOUNT_ABOVE_THRESHOLD`. Rule 2 (cross-border flag) always applies to
whichever event is emitted.

## Outbound event: `FraudCheckRequested`

| Field | Type | Notes |
|-------|------|-------|
| `orderId` | String | from the order |
| `accountId` | String | from the order |
| `amount` | Money | from the order, same currency |
| `crossBorder` | boolean | per Rule 2 |
| `reason` | FraudReason | which rule triggered the check |

## Acceptance criteria

**Rule 1**
- An order of exactly 1000 EUR does **not** trigger a fraud check (strictly greater).
- An order of 1000.01 EUR **does** trigger a fraud check.
- An order of 1500 USD triggers a fraud check (above the USD threshold of 1000).
- An order of 500 EUR does not trigger a fraud check.

**Rule 2**
- A triggered order shipping to a different country than the account has `crossBorder = true`.
- A triggered order shipping to the same country has `crossBorder = false`.

**Rule 3**
- An order of 101 EUR from an account created 12 hours before `placedAt` triggers
  a fraud check with `reason = RECENT_ACCOUNT_LOW_AMOUNT`.
- An order of exactly 100 EUR from a recent account does **not** trigger (strictly greater than 100).
- An order of 101 EUR from an account created **exactly** 24 hours before `placedAt`
  does **not** trigger (strictly less than 24h).
- An order of 101 EUR from an account created 25 hours before `placedAt` does **not** trigger.

**Rule precedence**
- An order of 1500 EUR from an account created 12 hours before `placedAt` triggers with
  `reason = AMOUNT_ABOVE_THRESHOLD` (Rule 1 wins over Rule 3).

**General**
- No fraud check is published for an order that matches no rule.

## Out of scope (for now)

- Deciding the fraud outcome (handled by a downstream service).
- Currency conversion between currencies.
- Persistence — this service is stateless.
