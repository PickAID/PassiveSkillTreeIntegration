package com.pickaid.passiveintegration.compat.carrier.binding;

import com.pickaid.passiveintegration.compat.carrier.CarrierKind;
import daripher.skilltree.skill.requirement.SkillRequirement;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record RequirementCarrierBinding(
        Supplier<SkillRequirement.Serializer> serializerFactory,
        Consumer<Supplier<SkillRequirement.Serializer>> serializerBinder
) implements CarrierBinding {
    @Override
    public CarrierKind kind() {
        return CarrierKind.REQUIREMENT;
    }
}
