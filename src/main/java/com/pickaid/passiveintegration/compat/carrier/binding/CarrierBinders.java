package com.pickaid.passiveintegration.compat.carrier.binding;

import com.pickaid.passiveintegration.compat.carrier.CarrierActivation;
import com.pickaid.passiveintegration.compat.carrier.CarrierEntry;
import com.pickaid.passiveintegration.compat.carrier.CarrierKind;
import daripher.skilltree.init.PSTEventListeners;
import daripher.skilltree.init.PSTItemBonuses;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.init.PSTSkillRequirements;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CarrierBinders {
    private final Map<CarrierKind, CarrierBinder> binders;

    public CarrierBinders(IEventBus modBus) {
        this(List.of(
                new SerializerCarrierBinder(CarrierKind.SKILL_BONUS, PSTSkillBonuses.REGISTRY),
                new SerializerCarrierBinder(CarrierKind.ITEM_BONUS, PSTItemBonuses.REGISTRY),
                new SerializerCarrierBinder(CarrierKind.EVENT_LISTENER, PSTEventListeners.REGISTRY),
                new SerializerCarrierBinder(CarrierKind.REQUIREMENT, PSTSkillRequirements.REGISTRY)
        ));
        binders.values().forEach(binder -> {
            if (binder instanceof SerializerCarrierBinder serializerBinder) {
                serializerBinder.register(modBus);
            }
        });
    }

    public CarrierBinders(List<CarrierBinder> binders) {
        Objects.requireNonNull(binders, "binders");
        Map<CarrierKind, CarrierBinder> binderMap = new EnumMap<>(CarrierKind.class);
        for (CarrierBinder binder : binders) {
            CarrierBinder previous = binderMap.putIfAbsent(binder.kind(), binder);
            if (previous != null) {
                throw new IllegalStateException("Duplicate carrier binder: " + binder.kind());
            }
        }
        this.binders = Map.copyOf(binderMap);
    }

    public CarrierActivation bind(CarrierEntry entry) {
        CarrierBinder binder = binders.get(entry.kind());
        if (binder == null) {
            return CarrierActivation.inactive(entry, "missing binder");
        }
        return binder.bind(entry);
    }
}
