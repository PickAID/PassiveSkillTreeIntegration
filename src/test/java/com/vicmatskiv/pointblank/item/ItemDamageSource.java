package com.vicmatskiv.pointblank.item;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class ItemDamageSource extends DamageSource {
    public ItemDamageSource(String msgId) {
        super(Holder.direct(new DamageType(msgId, 0.0F)));
    }
}
