package com.pickaid.passiveintegration.integration.gun;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public interface GunPlatformAdapter {
    String id();

    boolean matchesWeapon(ItemStack stack);

    boolean matchesProjectile(Entity entity);

    boolean matchesDamage(DamageSource source);
}
