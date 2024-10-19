package ru.timeconqueror.timecore.animation;

import ru.timeconqueror.timecore.animation.watcher.AbstractAnimationTicker;
import ru.timeconqueror.timecore.api.animation.AnimationScript;

public interface AnimationController {
    boolean startAnimationScript(AnimationScript animationScript, long clockTime);

    void removeAnimation(long clockTime, int transitionTime);

    void setCurrentTicker(AbstractAnimationTicker ticker);
}
