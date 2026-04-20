package com.pickaid.passiveintegration.compat.carrier.binding;

import com.pickaid.passiveintegration.compat.carrier.CarrierActivation;
import com.pickaid.passiveintegration.compat.carrier.CarrierEntry;
import com.pickaid.passiveintegration.compat.carrier.CarrierKind;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.event.SkillEventListener;
import daripher.skilltree.skill.bonus.item.ItemBonus;
import daripher.skilltree.skill.requirement.SkillRequirement;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;

public final class SerializerCarrierBinder implements CarrierBinder {
    private final CarrierKind kind;
    private final DeferredRegister<?> register;

    public SerializerCarrierBinder(CarrierKind kind, DeferredRegister<?> register) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.register = Objects.requireNonNull(register, "register");
    }

    @Override
    public CarrierKind kind() {
        return kind;
    }

    public void register(IEventBus modBus) {
        register.register(modBus);
    }

    @Override
    public CarrierActivation bind(CarrierEntry entry) {
        return switch (kind) {
            case SKILL_BONUS -> bindSkillBonus(entry);
            case ITEM_BONUS -> bindItemBonus(entry);
            case EVENT_LISTENER -> bindEventListener(entry);
            case REQUIREMENT -> bindRequirement(entry);
        };
    }

    @SuppressWarnings("unchecked")
    private CarrierActivation bindSkillBonus(CarrierEntry entry) {
        SkillBonusCarrierBinding binding = (SkillBonusCarrierBinding) entry.binding();
        RegistryObject<SkillBonus.Serializer> serializer = ((DeferredRegister<SkillBonus.Serializer>) register)
                .register(entry.id().getPath(), binding.serializerFactory());
        binding.serializerBinder().accept(serializer);
        return CarrierActivation.active(entry, "bound");
    }

    @SuppressWarnings("unchecked")
    private CarrierActivation bindItemBonus(CarrierEntry entry) {
        ItemBonusCarrierBinding binding = (ItemBonusCarrierBinding) entry.binding();
        RegistryObject<ItemBonus.Serializer> serializer = ((DeferredRegister<ItemBonus.Serializer>) register)
                .register(entry.id().getPath(), binding.serializerFactory());
        binding.serializerBinder().accept(serializer);
        return CarrierActivation.active(entry, "bound");
    }

    @SuppressWarnings("unchecked")
    private CarrierActivation bindEventListener(CarrierEntry entry) {
        EventListenerCarrierBinding binding = (EventListenerCarrierBinding) entry.binding();
        RegistryObject<SkillEventListener.Serializer> serializer = ((DeferredRegister<SkillEventListener.Serializer>) register)
                .register(entry.id().getPath(), binding.serializerFactory());
        binding.serializerBinder().accept(serializer);
        return CarrierActivation.active(entry, "bound");
    }

    @SuppressWarnings("unchecked")
    private CarrierActivation bindRequirement(CarrierEntry entry) {
        RequirementCarrierBinding binding = (RequirementCarrierBinding) entry.binding();
        RegistryObject<SkillRequirement.Serializer> serializer = ((DeferredRegister<SkillRequirement.Serializer>) register)
                .register(entry.id().getPath(), binding.serializerFactory());
        binding.serializerBinder().accept(serializer);
        return CarrierActivation.active(entry, "bound");
    }
}
