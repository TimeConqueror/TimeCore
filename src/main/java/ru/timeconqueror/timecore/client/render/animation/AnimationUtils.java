package ru.timeconqueror.timecore.client.render.animation;

import net.minecraft.client.renderer.Vector3f;
import ru.timeconqueror.timecore.api.client.render.animation.BlendType;
import ru.timeconqueror.timecore.api.client.render.animation.IAnimationLayer;
import ru.timeconqueror.timecore.client.render.model.TimeModelRenderer;

public class AnimationUtils {
    public static void applyRotation(TimeModelRenderer piece, IAnimationLayer layer, Vector3f rotationIn) {
        BlendType blendType = layer.getBlendType();
        rotationIn.mul(layer.getWeight());

        if (blendType == BlendType.OVERRIDE) {
            piece.rotateAngleX = piece.startRotationAngles.getX() + rotationIn.getX();
            piece.rotateAngleY = piece.startRotationAngles.getY() + rotationIn.getY();
            piece.rotateAngleZ = piece.startRotationAngles.getZ() + rotationIn.getZ();
        } else if (blendType == BlendType.ADDING) {
            piece.rotateAngleX += rotationIn.getX();
            piece.rotateAngleY += rotationIn.getY();
            piece.rotateAngleZ += rotationIn.getZ();
        } else throw new UnsupportedOperationException();
    }

    public static void applyOffset(TimeModelRenderer piece, IAnimationLayer layer, Vector3f offsetIn) {
        BlendType blendType = layer.getBlendType();
        offsetIn.mul(layer.getWeight());

        if (blendType == BlendType.OVERRIDE) {
            piece.offsetX = offsetIn.getX();
            piece.offsetY = offsetIn.getY();
            piece.offsetZ = offsetIn.getZ();
        } else if (blendType == BlendType.ADDING) {
            piece.offsetX += offsetIn.getX() - piece.offsetX;
            piece.offsetY += offsetIn.getY() - piece.offsetY;
            piece.offsetZ += offsetIn.getZ() - piece.offsetZ;
        } else throw new UnsupportedOperationException();
    }

    public static void applyScale(TimeModelRenderer piece, IAnimationLayer layer, Vector3f scaleIn) {
        BlendType blendType = layer.getBlendType();
        float weight = layer.getWeight();

        scaleIn.set(calcWeightedScale(scaleIn.getX(), weight),
                calcWeightedScale(scaleIn.getY(), weight),
                calcWeightedScale(scaleIn.getZ(), weight));

        if (blendType == BlendType.OVERRIDE) {
            piece.setScaleFactor(scaleIn.getX(), scaleIn.getY(), scaleIn.getZ());
        } else if (blendType == BlendType.ADDING) {
            Vector3f currentScale = piece.getScaleFactor();
            piece.setScaleFactor(scaleIn.getX() * currentScale.getX(), scaleIn.getY() * currentScale.getY(), scaleIn.getZ() * currentScale.getZ());
        } else throw new UnsupportedOperationException();
    }

    private static float calcWeightedScale(float scale, float weight) {
        return scale > 1 ? 1 + (scale - 1) * weight : (scale + (1 - scale) * weight);
    }
}
