package com.pickaid.passiveintegration;

import com.pickaid.passiveintegration.bootstrap.PassiveIntegrationBootstrap;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.logging.Logger;

@Mod(PassiveIntegration.MOD_ID)
public class PassiveIntegration {
    public static final String MOD_ID = "passiveintegration";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);

    public PassiveIntegration() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        PassiveIntegrationBootstrap.init(modBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
