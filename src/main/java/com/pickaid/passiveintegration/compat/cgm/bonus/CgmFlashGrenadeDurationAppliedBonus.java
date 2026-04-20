package com.pickaid.passiveintegration.compat.cgm.bonus;

import com.google.gson.JsonObject;
import com.pickaid.passiveintegration.compat.cgm.CgmGrenadeDurationCurves;
import daripher.skilltree.skill.bonus.SkillBonus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.text.DecimalFormat;
import java.util.function.Supplier;

public final class CgmFlashGrenadeDurationAppliedBonus implements SkillBonus<CgmFlashGrenadeDurationAppliedBonus> {
    private static Supplier<SkillBonus.Serializer> serializerLookup = () -> {
        throw new IllegalStateException("CGM flash duration serializer not bound yet");
    };

    public static void bindSerializer(Supplier<SkillBonus.Serializer> serializerSupplier) {
        serializerLookup = serializerSupplier;
    }

    private final double levels;

    public CgmFlashGrenadeDurationAppliedBonus(double levels) {
        this.levels = levels;
    }

    public double levels() {
        return levels;
    }

    public double durationMultiplier() {
        return CgmGrenadeDurationCurves.appliedMultiplier(levels);
    }

    @Override
    public boolean canMerge(SkillBonus<?> other) {
        return other instanceof CgmFlashGrenadeDurationAppliedBonus;
    }

    @Override
    public CgmFlashGrenadeDurationAppliedBonus merge(SkillBonus<?> other) {
        return new CgmFlashGrenadeDurationAppliedBonus(levels + ((CgmFlashGrenadeDurationAppliedBonus) other).levels);
    }

    @Override
    public CgmFlashGrenadeDurationAppliedBonus copy() {
        return new CgmFlashGrenadeDurationAppliedBonus(levels);
    }

    @Override
    public CgmFlashGrenadeDurationAppliedBonus multiply(double value) {
        return new CgmFlashGrenadeDurationAppliedBonus(levels * value);
    }

    @Override
    public SkillBonus.Serializer getSerializer() {
        return serializerLookup.get();
    }

    @Override
    public String getDescriptionId() {
        return "bonus.passiveintegration.cgm_flash_grenade_duration_applied_bonus";
    }

    @Override
    public MutableComponent getTooltip() {
        double extraPercent = (durationMultiplier() - 1.0D) * 100.0D;
        return Component.translatable(getDescriptionId(), new DecimalFormat("0.##").format(extraPercent));
    }

    @Override
    public boolean isPositive() {
        return true;
    }

    @Override
    public void addEditorWidgets(daripher.skilltree.client.widget.editor.SkillTreeEditor editor, int y,
                                 java.util.function.Consumer<CgmFlashGrenadeDurationAppliedBonus> consumer) {
        throw new UnsupportedOperationException("PassiveIntegration does not own SkillTree editor widgets");
    }

    public static final class Serializer implements SkillBonus.Serializer {
        @Override
        public CgmFlashGrenadeDurationAppliedBonus deserialize(JsonObject json) {
            return new CgmFlashGrenadeDurationAppliedBonus(json.has("levels") ? json.get("levels").getAsDouble() : 1.0D);
        }

        @Override
        public void serialize(JsonObject json, SkillBonus<?> bonus) {
            json.addProperty("levels", ((CgmFlashGrenadeDurationAppliedBonus) bonus).levels);
        }

        @Override
        public CgmFlashGrenadeDurationAppliedBonus deserialize(CompoundTag tag) {
            return new CgmFlashGrenadeDurationAppliedBonus(tag.getDouble("Levels"));
        }

        @Override
        public CompoundTag serialize(SkillBonus<?> bonus) {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("Levels", ((CgmFlashGrenadeDurationAppliedBonus) bonus).levels);
            return tag;
        }

        @Override
        public CgmFlashGrenadeDurationAppliedBonus deserialize(FriendlyByteBuf buffer) {
            return new CgmFlashGrenadeDurationAppliedBonus(buffer.readDouble());
        }

        @Override
        public void serialize(FriendlyByteBuf buffer, SkillBonus<?> bonus) {
            buffer.writeDouble(((CgmFlashGrenadeDurationAppliedBonus) bonus).levels);
        }

        @Override
        public SkillBonus<?> createDefaultInstance() {
            return new CgmFlashGrenadeDurationAppliedBonus(1.0D);
        }
    }
}
