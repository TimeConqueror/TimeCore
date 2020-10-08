package ru.timeconqueror.timecore.animation.action;

import net.minecraft.entity.Entity;
import ru.timeconqueror.timecore.TimeCore;
import ru.timeconqueror.timecore.animation.BaseAnimationManager;
import ru.timeconqueror.timecore.animation.PredefinedAnimation;
import ru.timeconqueror.timecore.animation.builders.PredefinedAnimations.EntityPredefinedAnimations;

public class EntityActionManager<T extends Entity> extends ActionManagerImpl<T> {
    private final EntityPredefinedAnimations predefinedAnimations;

    public EntityActionManager(BaseAnimationManager animationManager, T entity, EntityPredefinedAnimations predefinedAnimations) {
        super(animationManager, entity);
        this.predefinedAnimations = predefinedAnimations;
    }

    public void onTick() {
        T entity = getBoundObject();
        if (entity.world.isRemote) {
            BaseAnimationManager animationManager = getAnimationManager();

            PredefinedAnimation walkingAnimation = predefinedAnimations.getWalkingAnimation();

            if (walkingAnimation != null) {
                if (animationManager.containsLayer(walkingAnimation.getLayerName())) {
                    // floats of movement can be almost the same (like 0 and 0.000000001), so entity moves a very short distance, which is invisible for eyes.
                    // this can be because of converting coords to bytes to send them to client.
                    // so checking if it's more than 1/256 of the block will fix the issue
                    boolean posChanged = Math.abs(entity.getPosX() - entity.prevPosX) >= 1 / 256F
                            || Math.abs(entity.getPosZ() - entity.prevPosZ) >= 1 / 256F;

                    if (posChanged) {
                        walkingAnimation.getAnimationStarter().startAt(animationManager, walkingAnimation.getLayerName());
                    } else {
                        animationManager.removeAnimation(walkingAnimation.getLayerName());
                    }
                } else {
                    TimeCore.LOGGER.error("Walking animation for entity {} is set up to be displayed on layer '{}', but this layer doesn't exist.", entity.getClass(), walkingAnimation.getLayerName());
                }
            }
        }
    }
}
