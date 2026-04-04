package org.crychicteam.passiveintegration.mixins.conditions;

import daripher.skilltree.skill.bonus.condition.item.EquipmentCondition;
import net.minecraft.world.item.ItemStack;
import com.pickaid.passiveintegration.PassiveIntegration;
import org.crychicteam.passiveintegration.optional.cgm.OptionalBonusCgmAction;
import org.crychicteam.passiveintegration.optional.cgm.OptionalBonusTaczAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author M1hono.
 */
@Mixin(EquipmentCondition.class)
public class EquipmentConditionMixin {
    @Inject(method = "isRangedWeapon", at = @At("RETURN"), remap = false, cancellable = true)
    private static void passiveIntegration$isRangedWeapon(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (PassiveIntegration.isLoaded("cgm")) {
            cir.setReturnValue(OptionalBonusCgmAction.isRangedWeapon(stack) || cir.getReturnValue());
        }
        if (PassiveIntegration.isLoaded("tacz")) {
            cir.setReturnValue(OptionalBonusTaczAction.isRangedWeapon(stack) || cir.getReturnValue());
        }
        if (PassiveIntegration.isLoaded("pointblank")) {

        }
    }
}