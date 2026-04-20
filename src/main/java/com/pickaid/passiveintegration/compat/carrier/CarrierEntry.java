package com.pickaid.passiveintegration.compat.carrier;

import com.pickaid.passiveintegration.compat.carrier.binding.CarrierBinding;
import com.pickaid.passiveintegration.compat.carrier.binding.EventListenerCarrierBinding;
import com.pickaid.passiveintegration.compat.carrier.binding.ItemBonusCarrierBinding;
import com.pickaid.passiveintegration.compat.carrier.binding.RequirementCarrierBinding;
import com.pickaid.passiveintegration.compat.carrier.binding.SkillBonusCarrierBinding;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record CarrierEntry(
        CarrierKind kind,
        ResourceLocation id,
        String owner,
        CarrierGate gate,
        CarrierDomain domain,
        CarrierSemantic semantic,
        CarrierTarget target,
        String debugDescription,
        boolean requiresDatapackFixture,
        CarrierBinding binding
) {
    public CarrierEntry {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(gate, "gate");
        Objects.requireNonNull(domain, "domain");
        Objects.requireNonNull(semantic, "semantic");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(debugDescription, "debugDescription");
        Objects.requireNonNull(binding, "binding");
        if (binding.kind() != kind) {
            throw new IllegalArgumentException("binding kind mismatch: " + binding.kind() + " != " + kind);
        }
    }

    public static CarrierEntry skillBonus(
            ResourceLocation id,
            String owner,
            CarrierGate gate,
            CarrierDomain domain,
            CarrierSemantic semantic,
            CarrierTarget target,
            String debugDescription,
            boolean requiresDatapackFixture,
            SkillBonusCarrierBinding binding
    ) {
        return new CarrierEntry(CarrierKind.SKILL_BONUS, id, owner, gate, domain, semantic, target, debugDescription,
                requiresDatapackFixture, binding);
    }

    public static CarrierEntry itemBonus(
            ResourceLocation id,
            String owner,
            CarrierGate gate,
            CarrierDomain domain,
            CarrierSemantic semantic,
            CarrierTarget target,
            String debugDescription,
            boolean requiresDatapackFixture,
            ItemBonusCarrierBinding binding
    ) {
        return new CarrierEntry(CarrierKind.ITEM_BONUS, id, owner, gate, domain, semantic, target, debugDescription,
                requiresDatapackFixture, binding);
    }

    public static CarrierEntry eventListener(
            ResourceLocation id,
            String owner,
            CarrierGate gate,
            CarrierDomain domain,
            CarrierSemantic semantic,
            CarrierTarget target,
            String debugDescription,
            boolean requiresDatapackFixture,
            EventListenerCarrierBinding binding
    ) {
        return new CarrierEntry(CarrierKind.EVENT_LISTENER, id, owner, gate, domain, semantic, target, debugDescription,
                requiresDatapackFixture, binding);
    }

    public static CarrierEntry requirement(
            ResourceLocation id,
            String owner,
            CarrierGate gate,
            CarrierDomain domain,
            CarrierSemantic semantic,
            CarrierTarget target,
            String debugDescription,
            boolean requiresDatapackFixture,
            RequirementCarrierBinding binding
    ) {
        return new CarrierEntry(CarrierKind.REQUIREMENT, id, owner, gate, domain, semantic, target, debugDescription,
                requiresDatapackFixture, binding);
    }
}
