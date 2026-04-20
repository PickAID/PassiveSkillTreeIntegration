package com.pickaid.passiveintegration.bridge.carrier;

import com.pickaid.passiveintegration.compat.carrier.CarrierActivation;
import com.pickaid.passiveintegration.compat.carrier.CarrierRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class CarrierDebugBridge {
    private final CarrierRegistry registry;

    public CarrierDebugBridge(CarrierRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public List<CarrierActivation> entries() {
        return registry.activations();
    }

    public Optional<CarrierActivation> find(ResourceLocation id) {
        return registry.find(id);
    }
}
