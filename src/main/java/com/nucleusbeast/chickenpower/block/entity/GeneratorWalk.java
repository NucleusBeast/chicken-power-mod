package com.nucleusbeast.chickenpower.block.entity;

import com.mojang.serialization.Codec;
import net.minecraft.util.Mth;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** One server-selected walk. Clients can interpolate it from the level clock. */
public record GeneratorWalk(double x, double z, double targetX, double targetZ,
                            long startTick, float yaw, float startAngle, float startYaw, float startStride) {
    // Leave room for the half-scale adult's beak, including when facing a corner.
    static final double MARGIN = 0.32;
    private static final double SPEED = 0.012;
    private static final double DEGREES_PER_BLOCK = 120;
    static final double DEGREES_PER_FULL_TICK = SPEED * DEGREES_PER_BLOCK;
    static final GeneratorWalk IDLE = new GeneratorWalk(0.5, 0.5, 0.5, 0.5, 0, 0, 0, 0, 0);
    static final Codec<GeneratorWalk> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.fieldOf("x").forGetter(GeneratorWalk::x),
            Codec.DOUBLE.fieldOf("z").forGetter(GeneratorWalk::z),
            Codec.DOUBLE.fieldOf("targetX").forGetter(GeneratorWalk::targetX),
            Codec.DOUBLE.fieldOf("targetZ").forGetter(GeneratorWalk::targetZ),
            Codec.LONG.fieldOf("startTick").forGetter(GeneratorWalk::startTick),
            Codec.FLOAT.fieldOf("yaw").forGetter(GeneratorWalk::yaw),
            Codec.FLOAT.fieldOf("angle").forGetter(GeneratorWalk::startAngle),
            Codec.FLOAT.optionalFieldOf("startYaw", 0F).forGetter(GeneratorWalk::startYaw),
            Codec.FLOAT.optionalFieldOf("stride", 0F).forGetter(GeneratorWalk::startStride)
    ).apply(i, GeneratorWalk::new));

    public record Pose(double x, double z, float yaw, float gearAngle, float stride, boolean walking) {}

    public GeneratorWalk {
        // Saved walks can have endpoints from before the casing margin was increased.
        x = Math.clamp(x, MARGIN, 1 - MARGIN);
        z = Math.clamp(z, MARGIN, 1 - MARGIN);
        targetX = Math.clamp(targetX, MARGIN, 1 - MARGIN);
        targetZ = Math.clamp(targetZ, MARGIN, 1 - MARGIN);
    }

    public Pose sample(long tick, float partialTick) {
        double dx = targetX - x, dz = targetZ - z;
        double length = Math.hypot(dx, dz);
        double elapsed = Math.max(0, tick - startTick + (double) partialTick);
        double travelled = Math.min(length, elapsed * SPEED);
        double progress = length == 0 ? 1 : travelled / length;
        float turn = Mth.wrapDegrees(yaw - startYaw);
        float facing = startYaw + (float) Math.copySign(Math.min(Math.abs(turn), elapsed * 24), turn);
        // Oppose travel around the Y axis. On a radial path either spin direction is equivalent.
        double tangent = (x - 0.5) * dz - (z - 0.5) * dx;
        double spin = tangent > 1.0e-10 ? 1 : -1;
        double advance = travelled * DEGREES_PER_BLOCK;
        return new Pose(x + dx * progress, z + dz * progress, Mth.wrapDegrees(facing),
                (float) ((startAngle + spin * advance) % 360), (float) ((startStride + advance) % 360), travelled < length);
    }

    /** Unsigned rotation during the tick ending at tick, including a partial final step. */
    double degreesTurned(long tick) {
        if (tick <= startTick) return 0;
        double length = Math.hypot(targetX - x, targetZ - z);
        double remaining = length - (tick - startTick - 1) * SPEED;
        return Math.clamp(remaining, 0, SPEED) * DEGREES_PER_BLOCK;
    }

    GeneratorWalk towards(Pose from, long tick, double toX, double toZ, float facing) {
        return new GeneratorWalk(from.x, from.z, toX, toZ, tick, facing, from.gearAngle, from.yaw, from.stride);
    }

    static float heading(double dx, double dz) {
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }
}
