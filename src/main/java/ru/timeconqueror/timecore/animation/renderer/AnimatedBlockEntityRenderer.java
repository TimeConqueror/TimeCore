package ru.timeconqueror.timecore.animation.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import ru.timeconqueror.timecore.animation.AnimationSystem;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.client.render.model.IModelPuppeteer;
import ru.timeconqueror.timecore.api.client.render.model.ITimeModelRenderer;
import ru.timeconqueror.timecore.client.render.model.TimeModel;

public abstract class AnimatedBlockEntityRenderer<T extends BlockEntity & AnimatedObject<T>> implements BlockEntityRenderer<T>, ITimeModelRenderer<T> {
    private final ModelPuppeteer<T> puppeteer = new ModelPuppeteer<>();
    protected TimeModel model;

    public AnimatedBlockEntityRenderer(TimeModel model) {
        this.model = model;
    }

    @Override
    public void render(T blockEntity, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        getTimeModel().reset();

        AnimationSystem<T> system = blockEntity.animationSystem();
        if (system == null) return; // not loaded yet

        system.getAnimationManager().applyAnimations(getTimeModel(), partialTicks);
        puppeteer.processModel(blockEntity, model, partialTicks);

        ResourceLocation texture = getTexture(blockEntity);

        RenderType renderType = model.renderType(texture);

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5F, 0, 0.5F);

        int rgbaColor = getRgbaColor(blockEntity);
        model.renderToBuffer(matrixStackIn, bufferIn.getBuffer(renderType), combinedLightIn, combinedOverlayIn, rgbaColor);
        postRender(blockEntity, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, rgbaColor);
        matrixStackIn.popPose();
    }

    protected void postRender(T object, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packetLight, int packedOverlay, int rgbaColor) {

    }

    protected abstract ResourceLocation getTexture(T blockEntity);

    @Override
    public TimeModel getTimeModel() {
        return model;
    }

    @Override
    public IModelPuppeteer<T> getPuppeteer() {
        return puppeteer;
    }

    public int getRgbaColor(T blockEntity) {
        return 0xFFFFFFFF;
    }
}
