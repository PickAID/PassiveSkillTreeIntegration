package com.pickaid.passiveintegration.compat.carrier;

import com.pickaid.passiveintegration.compat.carrier.binding.CarrierBinders;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;

public final class CarrierRegistry {
    private final CarrierBinders binders;
    private final Map<ResourceLocation, CarrierActivation> activations = new LinkedHashMap<>();

    public CarrierRegistry(CarrierBinders binders) {
        this.binders = Objects.requireNonNull(binders, "binders");
    }

    public void bindAll(List<CarrierCatalog> catalogs) {
        Objects.requireNonNull(catalogs, "catalogs");
        for (CarrierCatalog catalog : catalogs) {
            for (CarrierEntry entry : catalog.entries()) {
                if (activations.containsKey(entry.id())) {
                    throw new IllegalStateException("Duplicate carrier id: " + entry.id());
                }
                CarrierGateResult gateResult = entry.gate().evaluate();
                CarrierActivation activation = gateResult.passed()
                        ? binders.bind(entry)
                        : CarrierActivation.inactive(entry, gateResult.reason());
                activations.put(entry.id(), activation);
            }
        }
    }

    public List<CarrierActivation> activations() {
        return List.copyOf(activations.values());
    }

    public Optional<CarrierActivation> find(ResourceLocation id) {
        return Optional.ofNullable(activations.get(id));
    }

    public void replaceActivations(List<CarrierActivation> replacement) {
        Objects.requireNonNull(replacement, "replacement");
        activations.clear();
        for (CarrierActivation activation : replacement) {
            activations.put(activation.id(), activation);
        }
    }
}
