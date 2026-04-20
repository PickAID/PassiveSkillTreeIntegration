package com.pickaid.passiveintegration.compat.carrier.binding;

import com.pickaid.passiveintegration.compat.carrier.CarrierKind;
import daripher.skilltree.skill.bonus.SkillBonus;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record SkillBonusCarrierBinding(
        Supplier<SkillBonus.Serializer> serializerFactory,
        Consumer<Supplier<SkillBonus.Serializer>> serializerBinder
) implements CarrierBinding {
    @Override
    public CarrierKind kind() {
        return CarrierKind.SKILL_BONUS;
    }
}
