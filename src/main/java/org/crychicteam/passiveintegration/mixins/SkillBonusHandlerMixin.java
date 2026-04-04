package org.crychicteam.passiveintegration.mixins;

import daripher.skilltree.skill.bonus.SkillBonusHandler;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import com.pickaid.passiveintegration.PassiveIntegration;
import org.crychicteam.passiveintegration.optional.cgm.OptionalBonusCgmAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkillBonusHandler.class)
public class SkillBonusHandlerMixin {
    @Inject(method = "applyCritBonuses(Lnet/minecraftforge/event/entity/living/LivingHurtEvent;)V",
            at = @At(value = "HEAD"),
            remap = false,
            cancellable = true)
    private static void passiveIntegration$applyCriticalBonuses(LivingHurtEvent event, CallbackInfo ci) {
        if (PassiveIntegration.isLoaded("cgm")) {
            if (OptionalBonusCgmAction.shouldCancelCritBonuses(event)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "applyArrowRetrievalBonus",
            at = @At(value = "HEAD"),
            remap = false)
    private static void passiveIntegration$applyArrowRetrievalBonus(LivingHurtEvent event, CallbackInfo ci) {
        if (PassiveIntegration.isLoaded("cgm")) {
            OptionalBonusCgmAction.handleArrowRetrieval(event);
        }
    }
}