package com.nucleusbeast.chickenpower.registry;

import com.nucleusbeast.chickenpower.ChickenPowerMod;
import com.nucleusbeast.chickenpower.block.ChickenGeneratorBlock;
import com.nucleusbeast.chickenpower.block.entity.ChickenGeneratorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ChickenPowerMod.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ChickenPowerMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ChickenPowerMod.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ChickenPowerMod.MOD_ID);

    //? if >=1.21.11 {
    public static final DeferredBlock<ChickenGeneratorBlock> CHICKEN_GENERATOR = BLOCKS.registerBlock(
            "chicken_generator", ChickenGeneratorBlock::new, ModRegistry::generatorBlockProperties);
    public static final DeferredBlock<Block> GENERATOR_CASING_BLOCK = BLOCKS.registerBlock(
            "generator_casing", Block::new, ModRegistry::generatorBlockProperties);
    //?} else {
    /*public static final DeferredBlock<ChickenGeneratorBlock> CHICKEN_GENERATOR = BLOCKS.registerBlock(
            "chicken_generator", ChickenGeneratorBlock::new, generatorBlockProperties(BlockBehaviour.Properties.of()));
    public static final DeferredBlock<Block> GENERATOR_CASING_BLOCK = BLOCKS.registerBlock(
            "generator_casing", Block::new, generatorBlockProperties(BlockBehaviour.Properties.of()));
    *///?}

    public static final DeferredItem<BlockItem> CHICKEN_GENERATOR_ITEM =
            ITEMS.registerSimpleBlockItem("chicken_generator", CHICKEN_GENERATOR);

    /** The gear of the chicken generator, as a separate part. */
    public static final DeferredItem<Item> GENERATOR_GEAR = ITEMS.registerSimpleItem("generator_gear");

    /** The base and glass casing of the chicken generator, without the gear. */
    public static final DeferredItem<BlockItem> GENERATOR_CASING =
            ITEMS.registerSimpleBlockItem("generator_casing", GENERATOR_CASING_BLOCK);

    /** The icon of the Chicken Power tab. It is not shown in the tab. */
    public static final DeferredItem<Item> TAB_ICON = ITEMS.registerSimpleItem("tab_icon");

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChickenGeneratorBlockEntity>> CHICKEN_GENERATOR_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("chicken_generator", () ->
                    //? if >=1.21.2 {
                    new BlockEntityType<>(ChickenGeneratorBlockEntity::new, CHICKEN_GENERATOR.get())
                    //?} else
                    //BlockEntityType.Builder.of(ChickenGeneratorBlockEntity::new, CHICKEN_GENERATOR.get()).build(null)
            );

    /** The Chicken Power tab. It shows every item that this mod registers, in registration order. */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CHICKEN_POWER_TAB =
            CREATIVE_TABS.register("chickenpower", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.chickenpower"))
                    .icon(() -> TAB_ICON.get().getDefaultInstance())
                    .displayItems((parameters, output) ->
                            ITEMS.getEntries().stream()
                                    .filter(item -> item != TAB_ICON)
                                    .forEach(item -> output.accept(item.get())))
                    .build());

    private ModRegistry() {}

    private static BlockBehaviour.Properties generatorBlockProperties(BlockBehaviour.Properties properties) {
        return properties
                .mapColor(MapColor.METAL)
                .strength(3.5F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.GLASS)
                .noOcclusion();
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        modEventBus.addListener(ModRegistry::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                //? if >=1.21.9 {
                Capabilities.Energy.BLOCK,
                //?} else
                //Capabilities.EnergyStorage.BLOCK,
                CHICKEN_GENERATOR_BLOCK_ENTITY.get(),
                (generator, side) -> side == Direction.DOWN ? generator.energyOutput() : null);
    }
}
