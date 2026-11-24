package com.nucleusbeast.chickenpower.block;

import com.mojang.serialization.MapCodec;
import com.nucleusbeast.chickenpower.block.entity.ChickenGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import com.nucleusbeast.chickenpower.registry.ModRegistry;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.nucleusbeast.chickenpower.block.entity.GeneratorWalk;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
//? if <1.21.2
//import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The static casing is the normal block model.
 * The spinning gear is drawn by {@code ChickenGeneratorRenderer}.
 */
public class ChickenGeneratorBlock extends BaseEntityBlock {
    //? if <26.3 {
    /*public static final MapCodec<ChickenGeneratorBlock> CODEC = simpleCodec(ChickenGeneratorBlock::new);
    *///?}

    public ChickenGeneratorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.OCCUPIED, false).setValue(BlockStateProperties.POWER, 0));
    }

    //? if <26.3 {
    /*@Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    *///?}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.OCCUPIED, BlockStateProperties.POWER);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return side.getAxis().isHorizontal() ? state.getValue(BlockStateProperties.POWER) : 0;
    }

    @Override
    //? if >=1.21.2 {
    protected InteractionResult useItemOn(
    //?} else
    //protected ItemInteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Items.EGG)) return super.useItemOn(stack, state, level, pos, player, hand, hit);
        if (level instanceof ServerLevel server && !state.getValue(BlockStateProperties.OCCUPIED)) {
            stack.consume(1, player);
            server.playSound(null, pos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.5F, 1.4F);
            if (server.getRandom().nextInt(8) == 0) {
                server.setBlock(pos, state.setValue(BlockStateProperties.OCCUPIED, true), Block.UPDATE_ALL);
            }
        }
        //? if >=1.21.2 {
        return InteractionResult.SUCCESS;
        //?} else
        //return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty() || player.getOffhandItem().is(Items.EGG)) {
            return super.useWithoutItem(state, level, pos, player, hit);
        }
        if (level instanceof ServerLevel && level.getBlockEntity(pos) instanceof ChickenGeneratorBlockEntity generator) {
            int rate = generator.productionLastSecond(level.getGameTime());
            int segments = 12;
            int filled = (int) Math.ceil((double) segments * rate / ChickenGeneratorBlockEntity.peakProductionPerSecond());
            var bar = Component.literal("|".repeat(filled)).withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("|".repeat(segments - filled)).withStyle(ChatFormatting.DARK_GRAY));
            var message = Component.translatable("hud.chickenpower.production", bar, rate);
            //? if >=26.1 {
            player.sendOverlayMessage(message);
            //?} else
            //player.displayClientMessage(message, true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || !state.getValue(BlockStateProperties.OCCUPIED)) return null;
        return createTickerHelper(type, ModRegistry.CHICKEN_GENERATOR_BLOCK_ENTITY.get(), ChickenGeneratorBlockEntity::tick);
    }

    //? if <1.21.5 {
    /*@Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moved) {
        if (!moved && !next.is(this) && level instanceof ServerLevel server && state.getValue(BlockStateProperties.OCCUPIED)
                && level.getBlockEntity(pos) instanceof ChickenGeneratorBlockEntity generator) {
            playChickenDeath(server, pos, generator.sampleWalk(0));
        }
        super.onRemove(state, level, pos, next, moved);
    }
    *///?}

    public static void playChickenDeath(ServerLevel level, BlockPos pos, GeneratorWalk.Pose pose) {
        var chicken = ChickenGeneratorBlockEntity.createChicken(level, pos);
        chicken.setPos(pos.getX() + pose.x(), chicken.getY(), pos.getZ() + pose.z());
        chicken.setYRot(pose.yaw());
        chicken.setYBodyRot(pose.yaw());
        chicken.setYHeadRot(pose.yaw());
        chicken.setNoGravity(true);
        chicken.setNoAi(true);
        chicken.setHealth(0);
        // The already-dying chicken runs vanilla's 20-tick death animation without combat drops.
        level.addFreshEntity(chicken);
        //? if >=26.1 {
        var sound = chicken.get(net.minecraft.core.component.DataComponents.CHICKEN_SOUND_VARIANT).value().adultSounds().deathSound().value();
        //?} else
        //var sound = SoundEvents.CHICKEN_DEATH;
        level.playSound(null, pos, sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChickenGeneratorBlockEntity(pos, state);
    }
}
