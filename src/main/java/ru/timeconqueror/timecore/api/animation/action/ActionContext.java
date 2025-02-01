package ru.timeconqueror.timecore.api.animation.action;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.timeconqueror.timecore.animation.AnimationSystem;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.animation.AnimationTicker;

@Getter
@AllArgsConstructor
public class ActionContext<T extends AnimatedObject<T>> {
    private final AnimationTicker ticker;
    private final T owner;
    private final long clockTime;
    private final long lastAnimationCycleIndex;

    public boolean isServerSide() {
        return !isClientSide();
    }

    public boolean isClientSide() {
        return animationSystem().isClientSide();
    }

    public AnimationSystem<T> animationSystem() {
        return owner.animationSystem();
    }
}