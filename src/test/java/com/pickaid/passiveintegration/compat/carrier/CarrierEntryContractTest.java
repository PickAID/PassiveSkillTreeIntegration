package com.pickaid.passiveintegration.compat.carrier;

import com.pickaid.passiveintegration.PassiveIntegration;
import com.pickaid.passiveintegration.compat.carrier.binding.ItemBonusCarrierBinding;
import com.pickaid.passiveintegration.compat.carrier.binding.SkillBonusCarrierBinding;
import com.pickaid.passiveintegration.optional.CarrierGates;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.item.ItemBonus;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarrierEntryContractTest {
    @Test
    void skillBonusEntryCarriesSemanticTargetAndFixtureFlag() {
        AtomicReference<Supplier<SkillBonus.Serializer>> serializerRef = new AtomicReference<>();
        SkillBonusCarrierBinding binding = new SkillBonusCarrierBinding(DummySkillBonusSerializer::new, serializerRef::set);
        CarrierEntry entry = CarrierEntry.skillBonus(
                PassiveIntegration.id("cgm_stun_grenade_duration_taken_reduction"),
                "cgm",
                CarrierGates.all(
                        CarrierGates.modLoaded("cgm"),
                        CarrierGates.classPresent("com.mrcrayfish.guns.entity.ThrowableStunGrenadeEntity")
                ),
                CarrierDomain.GRENADE,
                CarrierSemantic.CONTROL_DURATION_TAKEN,
                CarrierTarget.SELF,
                "Reduce CGM stun duration taken",
                true,
                binding
        );
        Supplier<SkillBonus.Serializer> serializerFactory = binding.serializerFactory();
        SkillBonus.Serializer serializer = serializerFactory.get();
        binding.serializerBinder().accept(serializerFactory);

        assertEquals(CarrierKind.SKILL_BONUS, entry.kind());
        assertEquals(new ResourceLocation("passiveintegration", "cgm_stun_grenade_duration_taken_reduction"), entry.id());
        assertEquals("cgm", entry.owner());
        assertEquals(CarrierDomain.GRENADE, entry.domain());
        assertEquals(CarrierSemantic.CONTROL_DURATION_TAKEN, entry.semantic());
        assertEquals(CarrierTarget.SELF, entry.target());
        assertTrue(entry.requiresDatapackFixture());
        assertSame(binding, entry.binding());
        assertTrue(serializer instanceof DummySkillBonusSerializer);
        assertSame(serializerFactory, serializerRef.get());
    }

    @Test
    void failedFactoryCarriesFailureReason() {
        CarrierGateResult result = CarrierGateResult.failed("missing mod");

        assertFalse(result.passed());
        assertEquals("missing mod", result.reason());
    }

    @Test
    void entryRejectsMismatchedBindingKind() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CarrierEntry(
                CarrierKind.SKILL_BONUS,
                PassiveIntegration.id("mismatched_binding"),
                "cgm",
                CarrierGates.always(),
                CarrierDomain.GRENADE,
                CarrierSemantic.CONTROL_DURATION_TAKEN,
                CarrierTarget.SELF,
                "Mismatched binding kind",
                false,
                new ItemBonusCarrierBinding(DummyItemBonusSerializer::new, supplier -> {
                })
        ));

        assertEquals("binding kind mismatch: ITEM_BONUS != SKILL_BONUS", exception.getMessage());
    }

    @Test
    void composedGateReturnsFirstFailureReason() {
        CarrierGateResult result = CarrierGates.all(
                CarrierGates.failed("missing mod"),
                CarrierGates.failed("missing class")
        ).evaluate();

        assertFalse(result.passed());
        assertEquals("missing mod", result.reason());
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

    private static final class DummyItemBonusSerializer implements ItemBonus.Serializer {
        @Override
        public ItemBonus deserialize(com.google.gson.JsonObject json) {
            throw new UnsupportedOperationException("test-only");
        }

        @Override
        public void serialize(com.google.gson.JsonObject json, ItemBonus value) {
            throw new UnsupportedOperationException("test-only");
        }

        @Override
        public ItemBonus deserialize(net.minecraft.nbt.CompoundTag tag) {
            throw new UnsupportedOperationException("test-only");
        }

        @Override
        public net.minecraft.nbt.CompoundTag serialize(ItemBonus value) {
            throw new UnsupportedOperationException("test-only");
        }

        @Override
        public ItemBonus deserialize(net.minecraft.network.FriendlyByteBuf buffer) {
            throw new UnsupportedOperationException("test-only");
        }

        @Override
        public void serialize(net.minecraft.network.FriendlyByteBuf buffer, ItemBonus value) {
            throw new UnsupportedOperationException("test-only");
        }

        @Override
        public ItemBonus createDefaultInstance() {
            throw new UnsupportedOperationException("test-only");
        }
    }
}
