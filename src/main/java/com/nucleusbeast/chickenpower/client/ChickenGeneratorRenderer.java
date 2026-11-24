package com.nucleusbeast.chickenpower.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nucleusbeast.chickenpower.ChickenPowerMod;
import com.nucleusbeast.chickenpower.block.entity.ChickenGeneratorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import com.nucleusbeast.chickenpower.block.entity.GeneratorWalk;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

//? if >=1.21.5
import net.minecraft.world.phys.Vec3;
//? if >=1.21.5
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
//? if >=1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderTypes;
//?} else
//import net.minecraft.client.renderer.RenderType;

//? if >=1.21.9 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
//?} else {
/*import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
*///?}

//? if >=26.1 {
import java.util.List;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.state.level.CameraRenderState;
//?} else if >=1.21.9 {
/*import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.state.CameraRenderState;
*///?} else if >=1.21.5 {
/*import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
*///?} else if >=1.21.4 {
/*import net.minecraft.client.resources.model.BakedModel;
*///?} else {
/*import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelIdentifier;
*///?}

/**
 * Draws the gear of the chicken generator and turns it around the vertical axis.
 * The glass casing and the base are part of the normal block model.
 */
public class ChickenGeneratorRenderer implements
        //? if >=1.21.9 {
        BlockEntityRenderer<ChickenGeneratorBlockEntity, ChickenGeneratorRenderer.State>
        //?} else
        //BlockEntityRenderer<ChickenGeneratorBlockEntity>
{
    public static final Identifier GEAR_MODEL_ID = ChickenPowerMod.id("block/chicken_generator_gear");

    //? if >=26.1 {
    public static final StandaloneModelKey<BlockStateModelPart> GEAR_MODEL = new StandaloneModelKey<>(GEAR_MODEL_ID::toString);
    //?} else if >=1.21.6 {
    /*public static final StandaloneModelKey<BlockStateModel> GEAR_MODEL = new StandaloneModelKey<>(GEAR_MODEL_ID::toString);
    *///?} else if >=1.21.5 {
    /*public static final StandaloneModelKey<BlockStateModel> GEAR_MODEL = new StandaloneModelKey<>(GEAR_MODEL_ID);
    *///?} else if <1.21.4 {
    /*public static final ModelIdentifier GEAR_MODEL = ModelIdentifier.standalone(GEAR_MODEL_ID);
    *///?}

    public ChickenGeneratorRenderer(BlockEntityRendererProvider.Context context) {}

    /** Turns the pose around the vertical center line of the block. */
    private static void applyGearRotation(PoseStack poseStack, float angle) {
        poseStack.rotateAround(Axis.YP.rotationDegrees(angle), 0.5F, 0.0F, 0.5F);
    }

    private static void positionChicken(PoseStack pose, GeneratorWalk.Pose walk) {
        pose.translate(walk.x(), 0.3125, walk.z());
        pose.rotateAround(Axis.YP.rotationDegrees(-walk.yaw()), 0, 0, 0);
    }

    //? if >=1.21.9 {
    public static class State extends BlockEntityRenderState {
        public GeneratorWalk.Pose walk;
        public LivingEntityRenderState chicken;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ChickenGeneratorBlockEntity blockEntity, State state, float partialTick, Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);
        state.walk = null;
        state.chicken = null;
        if (blockEntity.getBlockState().getValue(BlockStateProperties.OCCUPIED)) {
            state.walk = blockEntity.sampleWalk(partialTick);
            var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            state.chicken = (LivingEntityRenderState) dispatcher.extractEntity(blockEntity.getDisplayChicken(), partialTick);
            state.chicken.lightCoords = state.lightCoords;
            state.chicken.shadowPieces.clear();
            state.chicken.bodyRot = 0;
            state.chicken.yRot = 0;
            state.chicken.walkAnimationPos = state.walk.stride() * ((float) Math.PI / 15.0F);
            state.chicken.walkAnimationSpeed = state.walk.walking() ? 0.6F : 0;
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.chicken != null) {
            poseStack.pushPose();
            positionChicken(poseStack, state.walk);
            Minecraft.getInstance().getEntityRenderDispatcher().submit(state.chicken, camera, 0, 0, 0, poseStack, collector);
            poseStack.popPose();
        }
        var gear = Minecraft.getInstance().getModelManager().getStandaloneModel(GEAR_MODEL);
        if (gear == null) return;

        poseStack.pushPose();
        applyGearRotation(poseStack, state.walk == null ? 0 : state.walk.gearAngle());
        //? if >=26.1 {
        collector.submitBlockModel(poseStack, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), List.of(gear),
                new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        //?} else if >=1.21.11 {
        /*collector.submitBlockModel(poseStack, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), gear,
                1.0F, 1.0F, 1.0F, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        *///?} else {
        /*collector.submitBlockModel(poseStack, RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS), gear,
                1.0F, 1.0F, 1.0F, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        *///?}
        poseStack.popPose();
    }
    //?} else if >=1.21.5 {
    /*@Override
    public void render(ChickenGeneratorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay, Vec3 cameraPosition) {
        renderLegacy(blockEntity, partialTick, poseStack, buffers, packedLight, packedOverlay);
    }
    *///?} else {
    /*@Override
    public void render(ChickenGeneratorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        renderLegacy(blockEntity, partialTick, poseStack, buffers, packedLight, packedOverlay);
    }
    *///?}

    //? if <1.21.9 {
    /*// Renders the chicken and the gear directly to the buffers. Render states only exist from 1.21.9.
    private static void renderLegacy(ChickenGeneratorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                                     MultiBufferSource buffers, int packedLight, int packedOverlay) {
        GeneratorWalk.Pose walk = null;
        if (blockEntity.getBlockState().getValue(BlockStateProperties.OCCUPIED)) {
            walk = blockEntity.sampleWalk(partialTick);
            var chick = blockEntity.getDisplayChicken();
            int tick = (int) blockEntity.getLevel().getGameTime();
            if (chick.tickCount != tick) {
                chick.tickCount = tick;
                animateWalk(chick, walk.walking() ? 0.6F : 0);
            }
            poseStack.pushPose();
            positionChicken(poseStack, walk);
            renderEntity(chick, partialTick, poseStack, buffers, packedLight);
            poseStack.popPose();
        }

        poseStack.pushPose();
        applyGearRotation(poseStack, walk == null ? 0 : walk.gearAngle());
        renderGear(poseStack, buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS)), packedLight, packedOverlay);
        poseStack.popPose();
    }
    *///?}

    //? if >=1.21.2 && <1.21.9 {
    /*private static void animateWalk(LivingEntity entity, float speed) {
        entity.walkAnimation.update(speed, 1.0F, 1.0F);
    }

    private static void renderEntity(LivingEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0, 0, 0, partialTick, poseStack, buffers, light);
    }
    *///?} else if <1.21.2 {
    /*private static void animateWalk(LivingEntity entity, float speed) {
        entity.walkAnimation.update(speed, 1.0F);
    }

    private static void renderEntity(LivingEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0, 0, 0, 0, partialTick, poseStack, buffers, light);
    }
    *///?}

    //? if >=1.21.5 && <1.21.9 {
    /*private static void renderGear(PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer buffer, int light, int overlay) {
        BlockStateModel gear = Minecraft.getInstance().getModelManager().getStandaloneModel(GEAR_MODEL);
        if (gear == null) return;
        ModelBlockRenderer.renderModel(poseStack.last(), buffer, gear, 1.0F, 1.0F, 1.0F, light, overlay);
    }
    *///?} else if >=1.21.4 && <1.21.5 {
    /*private static void renderGear(PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer buffer, int light, int overlay) {
        BakedModel gear = Minecraft.getInstance().getModelManager().getStandaloneModel(GEAR_MODEL_ID);
        if (gear == null) return;
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(), buffer, null, gear, 1.0F, 1.0F, 1.0F, light, overlay);
    }
    *///?} else if <1.21.4 {
    /*private static void renderGear(PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer buffer, int light, int overlay) {
        BakedModel gear = Minecraft.getInstance().getModelManager().getModel(GEAR_MODEL);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(), buffer, null, gear, 1.0F, 1.0F, 1.0F, light, overlay);
    }
    *///?}
}
