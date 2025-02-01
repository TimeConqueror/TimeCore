package ru.timeconqueror.timecore.animation.watcher;

import ru.timeconqueror.timecore.animation.AnimationController;
import ru.timeconqueror.timecore.animation.AnimationData;
import ru.timeconqueror.timecore.animation.AnimationStarterImpl;
import ru.timeconqueror.timecore.animation.network.AnimationState;
import ru.timeconqueror.timecore.api.animation.Animation;
import ru.timeconqueror.timecore.api.animation.BlendType;
import ru.timeconqueror.timecore.api.client.render.model.ITimeModel;
import ru.timeconqueror.timecore.api.molang.TCMolangEnvironment;

public class EmptyAnimationTicker extends AbstractAnimationTicker {
    public static final EmptyAnimationTicker INSTANCE = new EmptyAnimationTicker();
    private static final AnimationData EMPTY = new AnimationStarterImpl(Animation.NULL).getData();

    private EmptyAnimationTicker() {
        super(new Timeline(0, 1, false, 0L, 0));
    }

    @Override
    public void apply(ITimeModel model, BlendType blendType, float outerWeight, TCMolangEnvironment environment, long systemTime) {

    }

    @Override
    public void update(AnimationController animationController, long clockTime) {

    }

    @Override
    public boolean canIgnore(AnimationData data) {
        return data == EMPTY;
    }

    @Override
    public boolean isTransition() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public AnimationData getAnimationData() {
        return EMPTY;
    }

    @Override
    public AnimationState getState(long clockTime) {
        return AnimationState.EmptyState.INSTANCE;
    }

    @Override
    public int getAnimationTimeAt(long clockTime) {
        return 0;
    }

    @Override
    public String print(long clockTime) {
        return "Empty";
    }
}
