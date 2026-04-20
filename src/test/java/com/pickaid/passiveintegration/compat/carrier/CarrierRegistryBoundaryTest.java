package com.pickaid.passiveintegration.compat.carrier;

import com.pickaid.passiveintegration.PassiveIntegration;
import com.pickaid.passiveintegration.compat.carrier.binding.CarrierBinder;
import com.pickaid.passiveintegration.compat.carrier.binding.CarrierBinders;
import com.pickaid.passiveintegration.compat.carrier.binding.SkillBonusCarrierBinding;
import daripher.skilltree.skill.bonus.SkillBonus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarrierRegistryBoundaryTest {
    @Test
    void gateFailureStaysVisibleAsInactiveActivation() {
        CarrierRegistry registry = new CarrierRegistry(new CarrierBinders(List.of(new FakeSkillBonusBinder())));
        CarrierEntry entry = sampleEntry("inactive", () -> CarrierGateResult.failed("missing mod: cgm"));

        registry.bindAll(List.<CarrierCatalog>of(() -> List.of(entry)));

        CarrierActivation activation = registry.activations().get(0);
        assertEquals(entry.id(), activation.id());
        assertFalse(activation.active());
        assertEquals("missing mod: cgm", activation.reason());
    }

    @Test
    void duplicateIdsFailFast() {
        CarrierRegistry registry = new CarrierRegistry(new CarrierBinders(List.of(new FakeSkillBonusBinder())));
        CarrierEntry first = sampleEntry("duplicate", () -> CarrierGateResult.PASSED);
        CarrierEntry second = sampleEntry("duplicate", () -> CarrierGateResult.PASSED);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> registry.bindAll(List.<CarrierCatalog>of(() -> List.of(first, second))));

        assertEquals("Duplicate carrier id: passiveintegration:duplicate", exception.getMessage());
    }

    @Test
    void successfulBindingUsesTheMatchingKindBinder() {
        FakeSkillBonusBinder binder = new FakeSkillBonusBinder();
        CarrierRegistry registry = new CarrierRegistry(new CarrierBinders(List.of(binder)));

        registry.bindAll(List.<CarrierCatalog>of(() -> List.of(sampleEntry("active", () -> CarrierGateResult.PASSED))));

        assertEquals(1, binder.bindCalls.get());
        assertTrue(registry.activations().get(0).active());
        assertEquals("bound", registry.activations().get(0).reason());
    }

    private static CarrierEntry sampleEntry(String path, CarrierGate gate) {
        return CarrierEntry.skillBonus(
                PassiveIntegration.id(path),
                "cgm",
                gate,
                CarrierDomain.GRENADE,
                CarrierSemantic.CONTROL_DURATION_TAKEN,
                CarrierTarget.SELF,
                "sample",
                true,
                new SkillBonusCarrierBinding(DummySkillBonusSerializer::new, supplier -> {
                })
        );
    }

    private static final class FakeSkillBonusBinder implements CarrierBinder {
        private final AtomicInteger bindCalls = new AtomicInteger();

        @Override
        public CarrierKind kind() {
            return CarrierKind.SKILL_BONUS;
        }

        @Override
        public CarrierActivation bind(CarrierEntry entry) {
            bindCalls.incrementAndGet();
            return CarrierActivation.active(entry, "bound");
        }
    }

    private static final class DummySkillBonusSerializer implements SkillBonus.Serializer {
        @Override
        public SkillBonus<?> deserialize(com.google.gson.JsonObject json) {
            throw new UnsupportedOperationException("test-only");
        }

        @Override
        public void serialize(com.google.gson.JsonObject json, SkillBonus<?> value) {
            throw new UnsupportedOperationException("test-only");
        }

        @Override
        public SkillBonus<?> deserialize(net.minecraft.nbt.CompoundTag tag) {
            throw new UnsupportedOperationException("test-only");
        }

        @Override
        public net.minecraft.nbt.CompoundTag serialize(SkillBonus<?> value) {
            throw new UnsupportedOperationException("test-only");
        }

        @Override
        public SkillBonus<?> deserialize(net.minecraft.network.FriendlyByteBuf buffer) {
            throw new UnsupportedOperationException("test-only");
        }

        @Override
        public void serialize(net.minecraft.network.FriendlyByteBuf buffer, SkillBonus<?> value) {
            throw new UnsupportedOperationException("test-only");
        }

        @Override
        public SkillBonus<?> createDefaultInstance() {
            throw new UnsupportedOperationException("test-only");
        }
    }
}
