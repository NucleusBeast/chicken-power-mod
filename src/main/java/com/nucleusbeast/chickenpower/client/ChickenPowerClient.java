package com.nucleusbeast.chickenpower.client;

import com.nucleusbeast.chickenpower.ChickenPowerMod;
import com.nucleusbeast.chickenpower.registry.ModRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
//? if >=1.21.6 {
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
//?} else if >=1.21.5
//import net.neoforged.neoforge.client.model.standalone.StandaloneModelBaker;

/** Client-only entry point. */
@Mod(value = ChickenPowerMod.MOD_ID, dist = Dist.CLIENT)
public class ChickenPowerClient {
    public ChickenPowerClient(IEventBus modEventBus) {
        modEventBus.addListener(ChickenPowerClient::registerRenderers);
        //? if >=1.21.5 {
        modEventBus.addListener(ChickenPowerClient::registerStandaloneModels);
        //?} else
        //modEventBus.addListener(ChickenPowerClient::registerAdditionalModels);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModRegistry.CHICKEN_GENERATOR_BLOCK_ENTITY.get(), ChickenGeneratorRenderer::new);
    }

    //? if >=26.1 {
    private static void registerStandaloneModels(ModelEvent.RegisterStandalone event) {
        event.register(ChickenGeneratorRenderer.GEAR_MODEL,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(ChickenGeneratorRenderer.GEAR_MODEL_ID));
    }
    //?} else if >=1.21.6 {
    /*private static void registerStandaloneModels(ModelEvent.RegisterStandalone event) {
        event.register(ChickenGeneratorRenderer.GEAR_MODEL,
                SimpleUnbakedStandaloneModel.blockStateModel(ChickenGeneratorRenderer.GEAR_MODEL_ID));
    }
    *///?} else if >=1.21.5 {
    /*private static void registerStandaloneModels(ModelEvent.RegisterStandalone event) {
        event.register(ChickenGeneratorRenderer.GEAR_MODEL, StandaloneModelBaker.blockStateModel());
    }
    *///?} else if >=1.21.4 {
    /*private static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(ChickenGeneratorRenderer.GEAR_MODEL_ID);
    }
    *///?} else {
    /*private static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(ChickenGeneratorRenderer.GEAR_MODEL);
    }
    *///?}
}
