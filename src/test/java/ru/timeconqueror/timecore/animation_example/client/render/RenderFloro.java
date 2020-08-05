package ru.timeconqueror.timecore.animation_example.client.render;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import ru.timeconqueror.timecore.TimeCore;
import ru.timeconqueror.timecore.animation_example.entity.FloroEntity;
import ru.timeconqueror.timecore.animation_example.registry.EntityRegistry;
import ru.timeconqueror.timecore.client.render.model.TimeEntityModel;
import ru.timeconqueror.timecore.client.render.model.TimeEntityRenderer;

public class RenderFloro extends TimeEntityRenderer<FloroEntity, TimeEntityModel<FloroEntity>> {
    public RenderFloro(EntityRendererManager rendererManager) {
        super(rendererManager, EntityRegistry.floroModel.setScaleMultiplier(1.6F), 0.5F);
    }

    @Override
    public ResourceLocation getEntityTexture(FloroEntity entity) {
        return new ResourceLocation(TimeCore.MODID, "textures/entity/floro.png");
    }
}