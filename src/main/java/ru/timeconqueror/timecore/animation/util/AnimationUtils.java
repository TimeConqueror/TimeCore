package ru.timeconqueror.timecore.animation.util;

import org.joml.Vector3f;
import ru.timeconqueror.timecore.api.animation.BlendType;
import ru.timeconqueror.timecore.client.render.model.TimeModelPart;

public class AnimationUtils {
    public static final int MILLIS_IN_TICK = 50;

    public static long millisToTicks(long milliSeconds) {
        return (long) Math.ceil((float) milliSeconds / MILLIS_IN_TICK);
    }

    public static long ticksToMillis(long ticks) {
        return ticks * MILLIS_IN_TICK;
    }

    public static long ticksToMillis(double ticks) {
        return Math.round(ticks * MILLIS_IN_TICK);
    }

    public static void applyRotation(TimeModelPart piece, BlendType blendType, float weight, Vector3f rotationIn) {
        rotationIn.mul(weight);

        if (blendType == BlendType.OVERWRITE) {
            piece.getRotation().set(piece.startRotationRadians);
        } else if (blendType != BlendType.ADD) throw new UnsupportedOperationException();

        piece.getRotation().add(rotationIn);
    }

    public static void applyOffset(TimeModelPart piece, BlendType blendType, float weight, Vector3f offsetIn) {
        offsetIn.mul(weight);

        if (blendType == BlendType.OVERWRITE) {
            piece.getTranslation().set(offsetIn);
        } else if (blendType == BlendType.ADD) {
            piece.getTranslation().add(offsetIn);
        } else throw new UnsupportedOperationException();
    }

    public static void applyScale(TimeModelPart piece, BlendType blendType, float weight, Vector3f scaleIn) {
        scaleIn.set(calcWeightedScale(scaleIn.x(), weight),
                calcWeightedScale(scaleIn.y(), weight),
                calcWeightedScale(scaleIn.z(), weight));

        if (blendType == BlendType.OVERWRITE) {
            piece.getScale().set(scaleIn.x(), scaleIn.y(), scaleIn.z());
        } else if (blendType == BlendType.ADD) {
            piece.getScale().mul(scaleIn);
        } else throw new UnsupportedOperationException();
    }

    private static float calcWeightedScale(float scale, float weight) {
        return scale > 1 ? 1 + (scale - 1) * weight : 1 - (1 - scale) * weight;
    }
}
