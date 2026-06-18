package voxxed_luxembourg.sdd.fraudcheck.domain;

/**
 * Reason why a fraud check was requested.
 * Extensible — new business rules add new values.
 */
public enum FraudReason {
    AMOUNT_ABOVE_THRESHOLD,
    RECENT_ACCOUNT_LOW_AMOUNT
}
