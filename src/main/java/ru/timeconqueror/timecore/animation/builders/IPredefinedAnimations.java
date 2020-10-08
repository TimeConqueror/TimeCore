package ru.timeconqueror.timecore.animation.builders;

import ru.timeconqueror.timecore.animation.AnimationStarter;

public interface IPredefinedAnimations {
    interface IEntityPredefinedAnimations extends IPredefinedAnimations {
        void setWalkingAnimation(AnimationStarter walkingAnimationStarter, String layerName);
    }
}