package ru.timeconqueror.timecore.api.animation;

import org.jetbrains.annotations.Nullable;
import ru.timeconqueror.timecore.animation.AnimationData;
import ru.timeconqueror.timecore.animation.AnimationStarterImpl;
import ru.timeconqueror.timecore.animation.component.LoopMode;

public interface AnimationStarter {
    static AnimationStarter copy(AnimationStarter starter) {
        return new AnimationStarterImpl(starter.getData());
    }

    static AnimationStarter of(Animation animation) {
        return new AnimationStarterImpl(animation);
    }

    static AnimationStarter of(AnimationData animationData) {
        return new AnimationStarterImpl(animationData);
    }

    /**
     * If set to true: does not apply the animation if there's the same animation on the layer.
     * Useful for walking animations, so you don't need to worry how to control animation endings.
     * Default: true.
     */
    AnimationStarter ignorable(boolean ignorable);

    /**
     * When you start this animation on the layer, which is playing the same animation, it won't be re-started.
     * This method forces the bound animation to be applied despite the animation which was played before.
     * Default: true.
     */
    AnimationStarter nonIgnorable();

    /**
     * In case the animation is ended and no any other animation come in its place, it will be transitioned to empty animation
     * within some time. This method allows to disable this automatic transition.
     * Default: false.
     */
    AnimationStarter withNoTransitionToNone();

    /**
     * Defines the time (in milliseconds) of the transition animation between the previous animation and the one we want to start.
     * Default: {@link AnimationConstants#BASIC_TRANSITION_TIME}.
     */
    AnimationStarter withTransitionTime(int transitionTime);

    /**
     * Sets the factor that will speed up or slow down the animation.
     * Default: 1F.
     */
    AnimationStarter withSpeed(float speedFactor);

    /**
     * Allows to run animation not from start, but from specific animation time (in milliseconds).
     * Default: 0.
     */
    AnimationStarter startingFrom(int animationTime);

    /**
     * Allows to run animation not from start, but from specific animation time.
     * As a parameter you need to present the percent [0.0; 1.0] of the animation to start from.
     * Default: 0.
     */
    AnimationStarter startingFrom(float animationTimePercentage);

    /**
     * Make the animation go backwards
     * Default: false
     */
    AnimationStarter reversed();

    /**
     * Make the animation go backwards
     * Default: false
     */
    AnimationStarter reversed(boolean reversed);

    /**
     * Overrides the loop mode which is provided by animation file.
     * <br>
     * <b color=yellow>Doesn't take any effect, if {@link AnimationScript.Builder#withNext(AnimationScript.Builder)} is called in a chain with not null parameter.</b>
     * Default: null
     */
    AnimationStarter withLoopMode(@Nullable LoopMode loopMode);

    AnimationData getData();
}
