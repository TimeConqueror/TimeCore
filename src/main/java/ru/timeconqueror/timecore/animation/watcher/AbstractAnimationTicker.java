package ru.timeconqueror.timecore.animation.watcher;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.timeconqueror.timecore.animation.AnimationController;
import ru.timeconqueror.timecore.animation.AnimationData;
import ru.timeconqueror.timecore.animation.network.AnimationState;
import ru.timeconqueror.timecore.api.animation.Animation;
import ru.timeconqueror.timecore.api.animation.AnimationTicker;
import ru.timeconqueror.timecore.api.animation.BlendType;
import ru.timeconqueror.timecore.api.client.render.model.ITimeModel;
import ru.timeconqueror.timecore.api.molang.TCMolangEnvironment;

@Getter
@RequiredArgsConstructor
public abstract class AbstractAnimationTicker implements AnimationTicker {

    private final Timeline timeline;

    public abstract void apply(ITimeModel model, BlendType blendType, float outerWeight, TCMolangEnvironment environment, long systemTime);

    public static AbstractAnimationTicker fromState(AnimationState state, long clockTime) {
        if (state instanceof AnimationState.EmptyState) {
            return EmptyAnimationTicker.INSTANCE;
        } else if (state instanceof AnimationState.ActiveState as) {
            return new AnimationTickerImpl(as, clockTime);
        } else if (state instanceof AnimationState.TransitionState ts) {
            return new TransitionTicker(ts, clockTime);
        } else {
            throw new IllegalArgumentException("Unknown state class: " + state.getClass());
        }
    }

    public abstract boolean canIgnore(AnimationData data);

    @Override
    public boolean isEmpty() {
        return this.getAnimationData().getAnimation() == Animation.NULL;
    }

    public abstract void update(AnimationController animationController, long clockTime);

    public abstract AnimationState getState(long clockTime);
}
