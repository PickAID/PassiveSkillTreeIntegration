package com.pickaid.passiveintegration.mixins.compat.cgm;

import com.pickaid.passiveintegration.compat.cgm.CgmGrenadeEffectHooks;
import com.mrcrayfish.guns.entity.ThrowableStunGrenadeEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(value = ThrowableStunGrenadeEntity.class, remap = false)
public abstract class ThrowableStunGrenadeEntityMixin {
    @Redirect(
            method = "calculateAndApplyEffect(Lnet/minecraft/world/effect/MobEffect;Lcom/mrcrayfish/guns/Config$EffectCriteria;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;DD)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"
            )
    )
    private boolean passiveintegration$redirectAddEffect(
            LivingEntity receiver,
            MobEffectInstance originalInstance,
            MobEffect effect,
            @Coerce Object criteria,
            LivingEntity target,
            Vec3 explosionPos,
            Vec3 targetEyePos,
            double distance,
            double angle
    ) {
        int adjustedDuration = CgmGrenadeEffectHooks.modifyDuration(
                (ThrowableProjectile) (Object) this,
                effect,
                target,
                originalInstance.getDuration()
        );
        return receiver.addEffect(new MobEffectInstance(
                effect,
                adjustedDuration,
                originalInstance.getAmplifier(),
                originalInstance.isAmbient(),
                originalInstance.isVisible()
        ));
    }
}
