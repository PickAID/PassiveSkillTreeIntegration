package com.pickaid.passiveintegration.compat.carrier;

public final class CarrierGateResult {
    public static final CarrierGateResult PASSED = new CarrierGateResult(true, "active");

    private final boolean passed;
    private final String reason;

    private CarrierGateResult(boolean passed, String reason) {
        this.passed = passed;
        this.reason = reason;
    }

    public static CarrierGateResult failed(String reason) {
        return new CarrierGateResult(false, reason);
    }

    public boolean passed() {
        return passed;
    }

    public String reason() {
        return reason;
    }
}
