package ru.timeconqueror.timecore.animation;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import ru.timeconqueror.timecore.animation.component.LoopMode;
import ru.timeconqueror.timecore.api.animation.Animation;
import ru.timeconqueror.timecore.api.animation.AnimationStarter;
import ru.timeconqueror.timecore.api.util.MathUtils;

import java.util.Objects;

//TODO changeable weight
//TODO mod dependent config for default animation data
//TODO check if loop mode work with reversed
public class AnimationStarterImpl implements AnimationStarter {
    private final AnimationData data;

    @ApiStatus.Internal
    public AnimationStarterImpl(Animation animation) {
        Objects.requireNonNull(animation);
        this.data = new AnimationData(animation);
    }

    @ApiStatus.Internal
    public AnimationStarterImpl(AnimationData animationData) {
        this.data = animationData.copy();
    }

    @Override
    public AnimationStarter ignorable(boolean ignorable) {
        this.data.ignorable = ignorable;
        return this;
    }

    @Override
    public AnimationStarter nonIgnorable() {
        this.data.ignorable = false;
        return this;
    }

    @Override
    public AnimationStarter withNoTransitionToNone() {
        this.data.noTransitionToNone = true;
        return this;
    }

    @Override
    public AnimationStarter withTransitionTime(int transitionTime) {
        data.transitionTime = Math.max(transitionTime, 0);
        return this;
    }

    @Override
    public AnimationStarter withSpeed(float speedFactor) {
        data.speed = Math.max(speedFactor, 0);
        return this;
    }

    @Override
    public AnimationStarter startingFrom(int animationTime) {
        data.startAnimationTime = MathUtils.coerceInRange(animationTime, 0, data.animation.getLength());
        return this;
    }

    @Override
    public AnimationStarter startingFrom(float animationTimePercentage) {
        animationTimePercentage = MathUtils.coerceInRange(animationTimePercentage, 0, 1);
        data.startAnimationTime = MathUtils.coerceInRange(Math.round(animationTimePercentage * data.animation.getLength()), 0, data.animation.getLength());
        return this;
    }

    @Override
    public AnimationStarter reversed() {
        data.reversed = true;
        return this;
    }

    @Override
    public AnimationStarter reversed(boolean reversed) {
        data.reversed = reversed;
        return this;
    }

    @Override
    public AnimationStarter withLoopMode(@Nullable LoopMode loopMode) {
        data.loopMode = loopMode;
        return this;
    }

    @Override
    public AnimationData getData() {
        return data;
    }

    @Override
    public String toString() {
        return data.toString();
    }

}
