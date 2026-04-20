package com.pickaid.passiveintegration.compat.carrier.binding;

import com.pickaid.passiveintegration.compat.carrier.CarrierKind;
import daripher.skilltree.skill.bonus.event.SkillEventListener;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record EventListenerCarrierBinding(
        Supplier<SkillEventListener.Serializer> serializerFactory,
        Consumer<Supplier<SkillEventListener.Serializer>> serializerBinder
) implements CarrierBinding {
    @Override
    public CarrierKind kind() {
        return CarrierKind.EVENT_LISTENER;
    }
}
