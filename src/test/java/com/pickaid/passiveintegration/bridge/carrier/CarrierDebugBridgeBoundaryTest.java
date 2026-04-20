package com.pickaid.passiveintegration.bridge.carrier;

import com.pickaid.passiveintegration.PassiveIntegration;
import com.pickaid.passiveintegration.compat.carrier.CarrierActivation;
import com.pickaid.passiveintegration.compat.carrier.CarrierDomain;
import com.pickaid.passiveintegration.compat.carrier.CarrierEntry;
import com.pickaid.passiveintegration.compat.carrier.CarrierGateResult;
import com.pickaid.passiveintegration.compat.carrier.CarrierRegistry;
import com.pickaid.passiveintegration.compat.carrier.CarrierSemantic;
import com.pickaid.passiveintegration.compat.carrier.CarrierTarget;
import com.pickaid.passiveintegration.compat.carrier.binding.CarrierBinder;
import com.pickaid.passiveintegration.compat.carrier.binding.CarrierBinders;
import com.pickaid.passiveintegration.compat.carrier.binding.RequirementCarrierBinding;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarrierDebugBridgeBoundaryTest {
    @Test
    void debugBridgeExposesActivationStateWithoutTouchingGameplayLogic() {
        CarrierRegistry registry = new CarrierRegistry(new CarrierBinders(List.<CarrierBinder>of()));
        CarrierActivation activation = CarrierActivation.inactive(
                CarrierEntry.requirement(
                        PassiveIntegration.id("debug_entry"),
                        "test",
                        () -> CarrierGateResult.PASSED,
                        CarrierDomain.GENERIC_COMBAT,
                        CarrierSemantic.TREE_REQUIREMENT,
                        CarrierTarget.SELF,
                        "debug-only",
                        false,
                        new RequirementCarrierBinding(() -> null, supplier -> {
                        })
                ),
                "missing binder"
        );
        registry.replaceActivations(List.of(activation));
        CarrierDebugBridge bridge = new CarrierDebugBridge(registry);

        assertEquals(1, bridge.entries().size());
        assertTrue(bridge.find(PassiveIntegration.id("debug_entry")).isPresent());
        assertEquals("missing binder", bridge.entries().get(0).reason());
    }
}
