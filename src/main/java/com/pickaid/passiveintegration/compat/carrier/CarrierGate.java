package com.pickaid.passiveintegration.compat.carrier;

@FunctionalInterface
public interface CarrierGate {
    CarrierGateResult evaluate();
}
