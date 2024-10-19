package ru.timeconqueror.timecore.api.animation;

import gg.moonflower.molangcompiler.api.MolangEnvironment;
import ru.timeconqueror.timecore.animation.action.AnimationEventListener;
import ru.timeconqueror.timecore.animation.watcher.AbstractAnimationTicker;

//TODO setWeight/setBlendType be synced
public interface Layer {
    float getWeight();

    BlendType getBlendType();

    MolangEnvironment getEnvironment();

    /**
     * Returns layer name.
     */
    String getName();

    AbstractAnimationTicker getCurrentTicker();

    void addAnimationEventListener(AnimationEventListener listener);

    void removeAnimationEventListener(AnimationEventListener listener);

//    /**
//     * Pauses the layer as well as the played animation.
//     * Being frozen layer will also ignore animation adding and removal
//     */
//    void freeze();
//
//    /**
//     * Unpauses the layer and the animation started on it.
//     */
//    void unfreeze();
//
//    /**
//     * Returns true if layer & animation on it is paused, otherwise returns false.
//     */
//    boolean isFrozen();
}
