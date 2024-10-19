package ru.timeconqueror.timecore.animation.predefined;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.entity.Entity;
import ru.timeconqueror.timecore.animation.AnimationSystem;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.animation.PredefinedAnimationManager;

@AllArgsConstructor
public class EntityPredefinedAnimationManager<T extends Entity & AnimatedObject<T>> implements PredefinedAnimationManager<T> {
    @Getter
    private final EntityPredefinedAnimations predefinedAnimations;

    @Override
    public void onTick(AnimationSystem<T> system, T entity) {
        if (entity.level().isClientSide()) {

            var walkingAnim = predefinedAnimations.getWalkingAnimation();
            var idleAnim = predefinedAnimations.getIdleAnimation();

            // floats of movement can be almost the same (f.e. 0 and 0.000000001), so entity moves a very short distance, which is invisible for eyes.
            // this can be because of converting coords to bytes to send them to client.
            // so checking if it's more than 1/256 of the block will fix the issue
            boolean posChanged = Math.abs(entity.getX() - entity.xo) >= 1 / 256F
                    || Math.abs(entity.getZ() - entity.zo) >= 1 / 256F;

            if (posChanged) {
                // if there is no walking anim or the idle and walking anim's layers aren't the same
                // we remove the idle animation
                if (idleAnim != null && (walkingAnim == null || !sameLayers(idleAnim, walkingAnim))) {
                    system.stopAnimation(idleAnim.getLayerName());
                }

                if (walkingAnim != null) {
                    system.startAnimation(walkingAnim.getStarter(), walkingAnim.getLayerName());
                }
            } else {
                // if there is no idle anim or the idle and walking anim's layers aren't the same
                // we remove the walking animation
                if (walkingAnim != null && (idleAnim == null || !sameLayers(idleAnim, walkingAnim))) {
                    system.stopAnimation(walkingAnim.getLayerName());
                }

                if (idleAnim != null) {
                    system.startAnimation(idleAnim.getStarter(), idleAnim.getLayerName());
                }
            }
        }
    }
}
