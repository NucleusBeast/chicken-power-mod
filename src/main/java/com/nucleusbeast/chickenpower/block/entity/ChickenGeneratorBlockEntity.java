package com.nucleusbeast.chickenpower.block.entity;

import com.nucleusbeast.chickenpower.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
//? if >=1.21.6 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?} else
//import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.Attributes;
//? if >=1.21.11 {
import net.minecraft.world.entity.animal.chicken.Chicken;
//?} else
//import net.minecraft.world.entity.animal.Chicken;
//? if >=26.2 {
import net.minecraft.world.entity.EntityTypes;
//?} else
//import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ChickenGeneratorBlockEntity extends BlockEntity {
    public ChickenGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.CHICKEN_GENERATOR_BLOCK_ENTITY.get(), pos, state);
    }

    private Chicken displayChicken;

    private GeneratorWalk walk = GeneratorWalk.IDLE;
    private boolean followingSeeds;
    private void syncWalk() {
        setChanged();
        var state = getBlockState();
        level.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_CLIENTS);
    }
    private final GeneratorEnergy energy = new GeneratorEnergy(this::setChanged);
    private double fractionalFE;
    public static final int TICKS_PER_SECOND = 20;
    private final int[] production = new int[TICKS_PER_SECOND];
    private long lastProductionTick = Long.MIN_VALUE;

    public int productionLastSecond(long now) {
        if (lastProductionTick == Long.MIN_VALUE || now - lastProductionTick >= TICKS_PER_SECOND) return 0;
        int total = 0;
        for (long tick = now - TICKS_PER_SECOND + 1; tick <= lastProductionTick; tick++) {
            total += production[Math.floorMod(tick, TICKS_PER_SECOND)];
        }
        return total;
    }

    private void recordProduction(long tick, int amount) {
        if (lastProductionTick != tick - 1) java.util.Arrays.fill(production, 0);
        production[Math.floorMod(tick, TICKS_PER_SECOND)] = amount;
        lastProductionTick = tick;
    }

    public static int peakProductionPerSecond() {
        return (int) Math.ceil(GeneratorWalk.DEGREES_PER_FULL_TICK * TICKS_PER_SECOND);
    }

    public GeneratorEnergy energyOutput() {
        return energy;
    }

    public GeneratorWalk.Pose sampleWalk(float partialTick) {
        return walk.sample(getLevel().getGameTime(), partialTick);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ChickenGeneratorBlockEntity generator) {
        long tick = level.getGameTime();
        double degrees = generator.walk.degreesTurned(tick);
        // One FE per degree. Carry partial units across ticks and direction changes.
        int storedBefore = generator.energy.stored();
        if (degrees > 0 && storedBefore < GeneratorEnergy.CAPACITY) {
            double total = generator.fractionalFE + degrees;
            int whole = (int) total;
            generator.energy.generate(whole);
            generator.fractionalFE = generator.energy.stored() == GeneratorEnergy.CAPACITY ? 0 : total - whole;
            generator.setChanged();
        }
        generator.recordProduction(tick, generator.energy.stored() - storedBefore);
        generator.energy.outputBelow(level, pos.below());
        int signal = (int) Math.ceil(15 * (degrees / GeneratorWalk.DEGREES_PER_FULL_TICK));
        if (signal != state.getValue(BlockStateProperties.POWER)) {
            state = state.setValue(BlockStateProperties.POWER, signal);
            level.setBlock(pos, state, Block.UPDATE_ALL);
            if (generator.isRemoved()) return;
        }
        // Vanilla mob goal selection runs every second tick.
        if (((tick + pos.getX() + pos.getY() + pos.getZ()) & 1) != 0) return;
        var from = generator.walk.sample(tick, 0);
        Player nearest = null;
        double rangeSquared = 100;
        for (Player player : level.players()) {
            if (player.isSpectator() || !player.isAlive()) continue;
            double distance = player.distanceToSqr(pos.getX() + from.x(), pos.getY() + 0.3125, pos.getZ() + from.z());
            if (distance > rangeSquared || !player.isHolding(stack -> stack.is(ItemTags.CHICKEN_FOOD))) continue;
            nearest = player;
            rangeSquared = distance;
        }

        double toX, toZ;
        float facing;
        if (nearest != null) {
            // Project the player onto the reachable square so walking around the casing leads the chicken around it.
            double dx = nearest.getX() - (pos.getX() + 0.5);
            double dz = nearest.getZ() - (pos.getZ() + 0.5);
            double scale = Math.max(1, Math.max(Math.abs(dx), Math.abs(dz)) / (0.5 - GeneratorWalk.MARGIN));
            toX = 0.5 + dx / scale;
            toZ = 0.5 + dz / scale;
            double lookX = nearest.getX() - pos.getX() - from.x();
            double lookZ = nearest.getZ() - pos.getZ() - from.z();
            facing = Math.hypot(lookX, lookZ) < 0.025 ? from.yaw() : GeneratorWalk.heading(lookX, lookZ);
            generator.followingSeeds = true;
            if (Math.hypot(toX - generator.walk.targetX(), toZ - generator.walk.targetZ()) < 0.006
                    && Math.abs(Mth.wrapDegrees(facing - generator.walk.yaw())) < 3) return;
        } else if (generator.followingSeeds) {
            generator.followingSeeds = false;
            toX = from.x();
            toZ = from.z();
            facing = from.yaw();
        } else {
            // RandomStrollGoal's default interval is 120, reduced to 60 per eligible goal check.
            if (from.walking() || level.getRandom().nextInt(60) != 0) return;
            double span = 1 - 2 * GeneratorWalk.MARGIN;
            toX = GeneratorWalk.MARGIN + level.getRandom().nextDouble() * span;
            toZ = GeneratorWalk.MARGIN + level.getRandom().nextDouble() * span;
            facing = GeneratorWalk.heading(toX - from.x(), toZ - from.z());
        }

        generator.walk = generator.walk.towards(from, tick, toX, toZ, facing);
        generator.syncWalk();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    //? if >=1.21.6 {
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("energy", energy.stored());
        output.putDouble("fractionalFE", fractionalFE);
        output.store("walk", GeneratorWalk.CODEC, walk);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energy.restore(input.getIntOr("energy", 0));
        fractionalFE = input.getDoubleOr("fractionalFE", 0);
        walk = input.read("walk", GeneratorWalk.CODEC).orElse(GeneratorWalk.IDLE);
    }
    //?} else if >=1.21.5 {
    /*@Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy.stored());
        tag.putDouble("fractionalFE", fractionalFE);
        tag.put("walk", GeneratorWalk.CODEC.encodeStart(NbtOps.INSTANCE, walk).getOrThrow());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy.restore(tag.getIntOr("energy", 0));
        fractionalFE = tag.getDoubleOr("fractionalFE", 0);
        walk = tag.contains("walk") ? GeneratorWalk.CODEC.parse(NbtOps.INSTANCE, tag.get("walk")).result().orElse(GeneratorWalk.IDLE)
                : GeneratorWalk.IDLE;
    }
    *///?} else {
    /*@Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy.stored());
        tag.putDouble("fractionalFE", fractionalFE);
        tag.put("walk", GeneratorWalk.CODEC.encodeStart(NbtOps.INSTANCE, walk).getOrThrow());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy.restore(tag.getInt("energy"));
        fractionalFE = tag.getDouble("fractionalFE");
        walk = tag.contains("walk") ? GeneratorWalk.CODEC.parse(NbtOps.INSTANCE, tag.get("walk")).result().orElse(GeneratorWalk.IDLE)
                : GeneratorWalk.IDLE;
    }
    *///?}

    //? if >=1.21.5 {
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (state.getValue(BlockStateProperties.OCCUPIED) && level instanceof net.minecraft.server.level.ServerLevel server) {
            com.nucleusbeast.chickenpower.block.ChickenGeneratorBlock.playChickenDeath(server, pos, sampleWalk(0));
        }
    }
    //?}

    public static Chicken createChicken(Level level, BlockPos pos) {
        //? if >=26.2 {
        Chicken chicken = new Chicken(EntityTypes.CHICKEN, level);
        //?} else
        //Chicken chicken = new Chicken(EntityType.CHICKEN, level);
        chicken.getAttribute(Attributes.SCALE).setBaseValue(0.50);
        chicken.setYHeadRot(0);
        chicken.setPos(pos.getX() + 0.5, pos.getY() + 0.3125, pos.getZ() + 0.5);
        return chicken;
    }

    /** Negative IDs for display-only chickens. Real entities get positive IDs, so these never clash. */
    private static final java.util.concurrent.atomic.AtomicInteger NEXT_DISPLAY_ID = new java.util.concurrent.atomic.AtomicInteger(-1);

    public Chicken getDisplayChicken() {
        if (displayChicken == null) {
            displayChicken = createChicken(getLevel(), getBlockPos());
            // This chicken is never added to the level, so it never gets an ID.
            // From 26.x, Entity.getId() throws when the ID is 0, and the renderer reads it.
            displayChicken.setId(NEXT_DISPLAY_ID.getAndDecrement());
        }
        return displayChicken;
    }
}
