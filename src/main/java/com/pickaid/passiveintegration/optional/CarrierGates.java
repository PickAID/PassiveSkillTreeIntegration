package com.pickaid.passiveintegration.optional;

import com.pickaid.passiveintegration.compat.carrier.CarrierGate;
import com.pickaid.passiveintegration.compat.carrier.CarrierGateResult;
import net.minecraftforge.fml.ModList;

public final class CarrierGates {
    private CarrierGates() {
    }

    public static CarrierGate always() {
        return () -> CarrierGateResult.PASSED;
    }

    public static CarrierGate failed(String reason) {
        return () -> CarrierGateResult.failed(reason);
    }

    public static CarrierGate modLoaded(String modId) {
        return () -> ModList.get().isLoaded(modId)
                ? CarrierGateResult.PASSED
                : CarrierGateResult.failed("missing mod: " + modId);
    }

    public static CarrierGate classPresent(String className) {
        return () -> {
            try {
                Class.forName(className, false, CarrierGates.class.getClassLoader());
                return CarrierGateResult.PASSED;
            } catch (ClassNotFoundException | LinkageError e) {
                return CarrierGateResult.failed("missing class: " + className);
            }
        };
    }

    public static CarrierGate all(CarrierGate... gates) {
        return () -> {
            for (CarrierGate gate : gates) {
                CarrierGateResult result = gate.evaluate();
                if (!result.passed()) {
                    return result;
                }
            }
            return CarrierGateResult.PASSED;
        };
    }
}
