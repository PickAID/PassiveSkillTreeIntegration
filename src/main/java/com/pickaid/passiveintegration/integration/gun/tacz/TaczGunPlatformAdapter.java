package com.pickaid.passiveintegration.integration.gun.tacz;

import com.pickaid.passiveintegration.integration.gun.GunPlatformAdapter;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public final class TaczGunPlatformAdapter implements GunPlatformAdapter {
    @Override
    public String id() {
        return "tacz";
    }

    @Override
    public boolean matchesWeapon(ItemStack stack) {
        return stack != null && !stack.isEmpty() && IGun.getIGunOrNull(stack) != null;
    }

    @Override
    public boolean matchesProjectile(Entity entity) {
        return entity instanceof EntityKineticBullet;
    }

    @Override
    public boolean matchesDamage(DamageSource source) {
        return source != null && matchesProjectile(source.getDirectEntity());
    }
}
