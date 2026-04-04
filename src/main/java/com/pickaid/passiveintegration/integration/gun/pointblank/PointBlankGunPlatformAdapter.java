package com.pickaid.passiveintegration.integration.gun.pointblank;

import com.pickaid.passiveintegration.integration.gun.GunPlatformAdapter;
import com.vicmatskiv.pointblank.entity.ProjectileLike;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.item.ItemDamageSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public final class PointBlankGunPlatformAdapter implements GunPlatformAdapter {
    @Override
    public String id() {
        return "pointblank";
    }

    @Override
    public boolean matchesWeapon(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof GunItem;
    }

    @Override
    public boolean matchesProjectile(Entity entity) {
        return entity instanceof ProjectileLike;
    }

    @Override
    public boolean matchesDamage(DamageSource source) {
        return source instanceof ItemDamageSource
                || source != null && matchesProjectile(source.getDirectEntity());
    }
}
