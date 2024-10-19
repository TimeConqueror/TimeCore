package ru.timeconqueror.timecore.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import ru.timeconqueror.timecore.animation.renderer.ModelConfiguration;
import ru.timeconqueror.timecore.api.client.render.model.ITimeModel;
import ru.timeconqueror.timecore.api.util.client.DrawHelper;

/**
 * For compatibility with generics which use {@link EntityModel}
 */
public class TimeEntityModel<T extends Entity> extends EntityModel<T> implements ITimeModel {
    private final TimeModel model;

    public TimeEntityModel(ModelConfiguration config) {
        super(config.renderTypeProvider());
        this.model = new TimeModel(config);
    }

    @Override
    public void reset() {
        model.reset();
    }

    @Override
    public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int light, int overlay, int rgbaColor) {
        renderToBuffer(poseStack, vertexConsumer, light, overlay,
                DrawHelper.getRed(rgbaColor) / 255F,
                DrawHelper.getGreen(rgbaColor) / 255F,
                DrawHelper.getBlue(rgbaColor) / 255F,
                DrawHelper.getAlpha(rgbaColor) / 255F);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        model.renderToBuffer(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }

    @Override
    public InFileLocation getLocation() {
        return model.getLocation();
    }

    @Override
    public @Nullable TimeModelPart tryGetPart(String partName) {
        return model.tryGetPart(partName);
    }

    @Override
    public TimeModelPart getPart(String partName) {
        return model.getPart(partName);
    }

    @Override
    public TimeModelPart getRoot() {
        return model.getRoot();
    }
}