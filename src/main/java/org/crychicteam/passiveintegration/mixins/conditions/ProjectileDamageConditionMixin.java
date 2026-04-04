package org.crychicteam.passiveintegration.mixins.conditions;

import daripher.skilltree.skill.bonus.condition.damage.ProjectileDamageCondition;
import net.minecraft.world.damagesource.DamageSource;
import com.pickaid.passiveintegration.PassiveIntegration;
import org.crychicteam.passiveintegration.optional.cgm.OptionalBonusCgmAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author M1hono.
 */
@Mixin(ProjectileDamageCondition.class)
public class ProjectileDamageConditionMixin {
    @Inject(method = "met", at = @At(value = "RETURN"), cancellable = true, remap = false)
    private void onProjectileDamageConditionMet(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (PassiveIntegration.isLoaded("cgm")) {
            cir.setReturnValue(OptionalBonusCgmAction.isProjectileDamage(source) || cir.getReturnValue());
        }
    }
}
