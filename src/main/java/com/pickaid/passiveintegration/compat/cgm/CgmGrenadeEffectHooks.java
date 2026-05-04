package com.pickaid.passiveintegration.compat.cgm;

import com.pickaid.passiveintegration.compat.cgm.bonus.CgmFlashGrenadeDurationAppliedBonus;
import com.pickaid.passiveintegration.compat.cgm.bonus.CgmStunGrenadeDurationTakenBonus;
import daripher.skilltree.skill.bonus.SkillBonusHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraftforge.registries.ForgeRegistries;

public final class CgmGrenadeEffectHooks {
    private static final ResourceLocation CGM_BLINDED = new ResourceLocation("cgm", "blinded");
    private static final ResourceLocation CGM_DEAFENED = new ResourceLocation("cgm", "deafened");

    private CgmGrenadeEffectHooks() {
    }

    public static int modifyDuration(ThrowableProjectile grenade, MobEffect effect, LivingEntity target, int baseDuration) {
        ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        double ownerLevels = 0.0D;
        double targetLevels = 0.0D;

        if (grenade.getOwner() instanceof Player thrower) {
            for (CgmFlashGrenadeDurationAppliedBonus bonus :
                    SkillBonusHandler.getMergedSkillBonuses(thrower, CgmFlashGrenadeDurationAppliedBonus.class)) {
                ownerLevels += bonus.levels();
            }
        }

        if (target instanceof Player player) {
            for (CgmStunGrenadeDurationTakenBonus bonus :
                    SkillBonusHandler.getMergedSkillBonuses(player, CgmStunGrenadeDurationTakenBonus.class)) {
                targetLevels += bonus.levels();
            }
        }

        return calculateDuration(effectId, baseDuration, ownerLevels, targetLevels);
    }

    static int calculateDuration(ResourceLocation effectId, int baseDuration, double ownerLevels, double targetLevels) {
        if (!CGM_BLINDED.equals(effectId) && !CGM_DEAFENED.equals(effectId)) {
            return baseDuration;
        }

        double multiplier = CgmGrenadeDurationCurves.appliedMultiplier(ownerLevels)
                * CgmGrenadeDurationCurves.takenMultiplier(targetLevels);
        long result = Math.round(baseDuration * multiplier);
        if (result < 0L) {
            return 0;
        }
        return (int) Math.min(Integer.MAX_VALUE, result);
    }
}
