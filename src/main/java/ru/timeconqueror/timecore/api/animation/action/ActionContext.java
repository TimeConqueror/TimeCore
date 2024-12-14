package ru.timeconqueror.timecore.api.animation.action;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.animation.AnimationTicker;

@Getter
@AllArgsConstructor
public class ActionContext<T extends AnimatedObject<T>> {
    private final AnimationTicker ticker;
    private final T owner;
    /**
     * Note: For non-looped animations clockTime is guaranteed to not exceed animation elapsed length.
     */
    private final long clockTime;
    private final long lastAnimationCycleIndex;
}