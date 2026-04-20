package com.pickaid.passiveintegration.compat.carrier;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record CarrierActivation(
        ResourceLocation id,
        CarrierKind kind,
        String owner,
        CarrierDomain domain,
        CarrierSemantic semantic,
        CarrierTarget target,
        boolean active,
        String reason
) {
    public CarrierActivation {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(domain, "domain");
        Objects.requireNonNull(semantic, "semantic");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(reason, "reason");
    }

    public static CarrierActivation active(CarrierEntry entry, String reason) {
        return new CarrierActivation(entry.id(), entry.kind(), entry.owner(), entry.domain(), entry.semantic(), entry.target(),
                true, reason);
    }

    public static CarrierActivation inactive(CarrierEntry entry, String reason) {
        return new CarrierActivation(entry.id(), entry.kind(), entry.owner(), entry.domain(), entry.semantic(), entry.target(),
                false, reason);
    }
}
