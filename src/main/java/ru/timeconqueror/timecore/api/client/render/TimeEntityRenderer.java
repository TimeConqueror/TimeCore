package ru.timeconqueror.timecore.api.client.render;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.LivingEntity;

public abstract class TimeEntityRenderer<T extends LivingEntity & IAnimationProvider, M extends TimeEntityModel<T>> extends LivingRenderer<T, M> {
    public TimeEntityRenderer(EntityRendererManager rendererManager, M entityModelIn, float shadowSizeIn) {
        super(rendererManager, entityModelIn, shadowSizeIn);
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        entity.getAnimationManager().processAnimations(getEntityModel());
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
