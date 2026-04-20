package com.pickaid.passiveintegration.compat.carrier.binding;

import com.pickaid.passiveintegration.compat.carrier.CarrierKind;
import daripher.skilltree.skill.bonus.item.ItemBonus;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record ItemBonusCarrierBinding(
        Supplier<ItemBonus.Serializer> serializerFactory,
        Consumer<Supplier<ItemBonus.Serializer>> serializerBinder
) implements CarrierBinding {
    @Override
    public CarrierKind kind() {
        return CarrierKind.ITEM_BONUS;
    }
}
