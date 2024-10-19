package ru.timeconqueror.timecore.animation.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.client.render.model.IModelPuppeteer;
import ru.timeconqueror.timecore.api.client.render.model.ITimeModelRenderer;
import ru.timeconqueror.timecore.client.render.model.TimeEntityModel;

public abstract class AnimatedLivingEntityRenderer<T extends LivingEntity & AnimatedObject<T>, M extends TimeEntityModel<T>> extends LivingEntityRenderer<T, M> implements ITimeModelRenderer<T> {
    private final ModelPuppeteer<T> puppeteer = new ModelPuppeteer<>();

    public AnimatedLivingEntityRenderer(EntityRendererProvider.Context ctx, M entityModelIn, float shadowSizeIn) {
        super(ctx, entityModelIn, shadowSizeIn);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        getModel().reset();

        entity.animationSystem().getAnimationManager().applyAnimations(getModel(), partialTicks);
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
    }

    @Override
    protected void setupRotations(T entityLiving, PoseStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks);
        matrixStackIn.scale(-1.0F, -1.0F, 1.0F); // to mirror models to a normal state
        puppeteer.processModel(entityLiving, getModel(), partialTicks);
    }

    @Override
    protected void scale(T entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime) {
        super.scale(entitylivingbaseIn, matrixStackIn, partialTickTime);

        matrixStackIn.translate(0, 1.501F, 0);//Mojang, WTF???
    }

    //copy from MobRenderer to prevent default name showing
    protected boolean shouldShowName(T entityIn) {
        return super.shouldShowName(entityIn) && (entityIn.shouldShowName() || entityIn.hasCustomName() && entityIn == this.entityRenderDispatcher.crosshairPickEntity);
    }

    @Override
    public TimeEntityModel<T> getTimeModel() {
        return getModel();
    }

    @Override
    public IModelPuppeteer<T> getPuppeteer() {
        return puppeteer;
    }
}
