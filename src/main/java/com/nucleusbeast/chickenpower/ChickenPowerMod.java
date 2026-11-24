package com.nucleusbeast.chickenpower;

import com.nucleusbeast.chickenpower.registry.ModRegistry;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ChickenPowerMod.MOD_ID)
public class ChickenPowerMod {
    public static final String MOD_ID = "chickenpower";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String VERSION = /*$ mod_version*/"0.1.0";
    public static final String MINECRAFT = /*$ minecraft*/"26.3";

    // See https://docs.neoforged.net/docs/gettingstarted/modfiles#javafml-and-mod
    public ChickenPowerMod(IEventBus modEventBus, ModContainer modContainer) {
        ModRegistry.register(modEventBus);
        LOGGER.info("Chicken Power loaded!");
    }

    /** Creates an {@link Identifier} in this mod's namespace. */
    public static Identifier id(String path) {
        return id(MOD_ID, path);
    }

    /**
     * Adapts to the {@link Identifier} changes introduced in 1.21.
     */
    public static Identifier id(String namespace, String path) {
        //? if <1.21 {
        /*return new Identifier(namespace, path);
        *///?} else
        return Identifier.fromNamespaceAndPath(namespace, path);
    }
}