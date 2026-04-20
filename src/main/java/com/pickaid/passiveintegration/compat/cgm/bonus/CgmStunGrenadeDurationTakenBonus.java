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

public final class CgmStunGrenadeDurationTakenBonus implements SkillBonus<CgmStunGrenadeDurationTakenBonus> {
    private static Supplier<SkillBonus.Serializer> serializerLookup = () -> {
        throw new IllegalStateException("CGM stun duration serializer not bound yet");
    };

    public static void bindSerializer(Supplier<SkillBonus.Serializer> serializerSupplier) {
        serializerLookup = serializerSupplier;
    }

    private final double levels;

    public CgmStunGrenadeDurationTakenBonus(double levels) {
        this.levels = levels;
    }

    public double levels() {
        return levels;
    }

    public double durationMultiplier() {
        return CgmGrenadeDurationCurves.takenMultiplier(levels);
    }

    @Override
    public boolean canMerge(SkillBonus<?> other) {
        return other instanceof CgmStunGrenadeDurationTakenBonus;
    }

    @Override
    public CgmStunGrenadeDurationTakenBonus merge(SkillBonus<?> other) {
        return new CgmStunGrenadeDurationTakenBonus(levels + ((CgmStunGrenadeDurationTakenBonus) other).levels);
    }

    @Override
    public CgmStunGrenadeDurationTakenBonus copy() {
        return new CgmStunGrenadeDurationTakenBonus(levels);
    }

    @Override
    public CgmStunGrenadeDurationTakenBonus multiply(double value) {
        return new CgmStunGrenadeDurationTakenBonus(levels * value);
    }

    @Override
    public SkillBonus.Serializer getSerializer() {
        return serializerLookup.get();
    }

    @Override
    public String getDescriptionId() {
        return "bonus.passiveintegration.cgm_stun_grenade_duration_taken_reduction";
    }

    @Override
    public MutableComponent getTooltip() {
        double reductionPercent = (1.0D - durationMultiplier()) * 100.0D;
        return Component.translatable(getDescriptionId(), new DecimalFormat("0.##").format(reductionPercent));
    }

    @Override
    public boolean isPositive() {
        return true;
    }

    @Override
    public void addEditorWidgets(daripher.skilltree.client.widget.editor.SkillTreeEditor editor, int y,
                                 java.util.function.Consumer<CgmStunGrenadeDurationTakenBonus> consumer) {
        throw new UnsupportedOperationException("PassiveIntegration does not own SkillTree editor widgets");
    }

    public static final class Serializer implements SkillBonus.Serializer {
        @Override
        public CgmStunGrenadeDurationTakenBonus deserialize(JsonObject json) {
            return new CgmStunGrenadeDurationTakenBonus(json.has("levels") ? json.get("levels").getAsDouble() : 1.0D);
        }

        @Override
        public void serialize(JsonObject json, SkillBonus<?> bonus) {
            json.addProperty("levels", ((CgmStunGrenadeDurationTakenBonus) bonus).levels);
        }

        @Override
        public CgmStunGrenadeDurationTakenBonus deserialize(CompoundTag tag) {
            return new CgmStunGrenadeDurationTakenBonus(tag.getDouble("Levels"));
        }

        @Override
        public CompoundTag serialize(SkillBonus<?> bonus) {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("Levels", ((CgmStunGrenadeDurationTakenBonus) bonus).levels);
            return tag;
        }

        @Override
        public CgmStunGrenadeDurationTakenBonus deserialize(FriendlyByteBuf buffer) {
            return new CgmStunGrenadeDurationTakenBonus(buffer.readDouble());
        }

        @Override
        public void serialize(FriendlyByteBuf buffer, SkillBonus<?> bonus) {
            buffer.writeDouble(((CgmStunGrenadeDurationTakenBonus) bonus).levels);
        }

        @Override
        public SkillBonus<?> createDefaultInstance() {
            return new CgmStunGrenadeDurationTakenBonus(1.0D);
        }
    }
}
