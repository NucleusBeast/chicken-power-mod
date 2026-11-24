package com.nucleusbeast.chickenpower.block.entity;

//? if >=1.21.9 {
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
//?} else
//import net.neoforged.neoforge.energy.EnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;

public final class GeneratorEnergy extends
        //? if >=1.21.9 {
        SimpleEnergyHandler
        //?} else
        //EnergyStorage
{
    static final int CAPACITY = 360;
    private final Runnable changed;

    GeneratorEnergy(Runnable changed) {
        super(CAPACITY, 0, CAPACITY);
        this.changed = changed;
    }

    int stored() {
        return energy;
    }

    void restore(int amount) {
        energy = Math.clamp(amount, 0, CAPACITY);
    }

    void generate(int amount) {
        restore(energy + amount);
    }

    //? if >=1.21.9 {
    @Override
    protected void onEnergyChanged(int previousAmount) {
        changed.run();
    }
    //?} else {
    /*@Override
    public int extractEnergy(int amount, boolean simulate) {
        int extracted = super.extractEnergy(Math.max(0, amount), simulate);
        if (!simulate && extracted > 0) changed.run();
        return extracted;
    }
    *///?}

    void outputBelow(Level level, BlockPos below) {
        if (energy == 0 || !level.hasChunkAt(below)) return;
        //? if >=1.21.9 {
        var receiver = level.getCapability(Capabilities.Energy.BLOCK, below, Direction.UP);
        EnergyHandlerUtil.move(this, receiver, energy, null);
        //?} else {
        /*var receiver = level.getCapability(Capabilities.EnergyStorage.BLOCK, below, Direction.UP);
        if (receiver != null && receiver.canReceive()) {
            extractEnergy(receiver.receiveEnergy(energy, false), false);
        }
        *///?}
    }
}
