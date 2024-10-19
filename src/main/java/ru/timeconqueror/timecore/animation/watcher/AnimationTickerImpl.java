package ru.timeconqueror.timecore.animation.watcher;

import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.object.MolangLibrary;
import lombok.Getter;
import ru.timeconqueror.timecore.animation.AnimationController;
import ru.timeconqueror.timecore.animation.AnimationData;
import ru.timeconqueror.timecore.animation.component.LoopMode;
import ru.timeconqueror.timecore.animation.network.AnimationState;
import ru.timeconqueror.timecore.api.animation.Animation;
import ru.timeconqueror.timecore.api.animation.AnimationConstants;
import ru.timeconqueror.timecore.api.animation.AnimationScript;
import ru.timeconqueror.timecore.api.animation.BlendType;
import ru.timeconqueror.timecore.api.client.render.model.ITimeModel;
import ru.timeconqueror.timecore.api.molang.Molang;
import ru.timeconqueror.timecore.molang.MolangObjects;

@Getter
public class AnimationTickerImpl extends AbstractAnimationTicker {
    private final AnimationScript animationScript;
    private final MolangLibrary tickerQuery;

    public AnimationTickerImpl(AnimationScript animationScript, long clockTime) {
        super(new Timeline(animationScript.getAnimationData().getAnimationLength(),
                animationScript.getAnimationData().getSpeed(),
                animationScript.getAnimationData().isReversed(),
                clockTime,
                animationScript.getAnimationData().getStartAnimationTime()));
        this.animationScript = animationScript;
        this.tickerQuery = MolangObjects.queriesForTicker(this);
    }

    public AnimationTickerImpl(AnimationState.ActiveState state, long clockTime) {
        this(state.getAnimationScript(), clockTime - state.getElapsedTime());
    }

    @Override
    public void update(AnimationController animationController, long clockTime) {
        long elapsedTimeTillFirstBoundary = getTimeline().getElapsedTimeTillFirstBoundary(clockTime);
        // if still playing the zero cycle and hasn't achieved the first boundary
        if (elapsedTimeTillFirstBoundary > 0) return;

        AnimationScript nextScript = animationScript.getNextScript();
        if (nextScript != null) {
            animationController.startAnimationScript(nextScript, clockTime + elapsedTimeTillFirstBoundary);
            return;
        }

        AnimationData data = getAnimationData();
        if (data.getLoopMode() == LoopMode.DO_NOT_LOOP) {
            animationController.removeAnimation(clockTime + elapsedTimeTillFirstBoundary, data.isNoTransitionToNone() ? 0 : AnimationConstants.BASIC_TRANSITION_TIME);
        }
    }

    @Override
    public void apply(ITimeModel model, BlendType blendType, float outerWeight, MolangEnvironment env, long clockTime) {
        Animation animation = getAnimationData().getAnimation();
        //TODO custom weight

        int animationTime = getTimeline().getAnimationTime(clockTime, isLooped());

        env.loadLibrary(Molang.Query.Domains.ANIMATION, tickerQuery);
        animation.apply(model, blendType, outerWeight, env, animationTime);
        env.unloadLibrary(Molang.Query.Domains.ANIMATION);
    }

    @Override
    public boolean canIgnore(AnimationData data) {
        return getAnimationData().equals(data);
    }

    @Override
    public boolean isTransition() {
        return false;
    }

    @Override
    public int getAnimationTimeAt(long clockTime) {
        return getTimeline().getAnimationTime(clockTime, isLooped());
    }

    public String print(long clockTime) {
        return String.format("Animation: Progress Time: %d/%d, Elapsed: %d/%dms, Data: %s", getAnimationTimeAt(clockTime), getAnimationLength(), getElapsedTimeAt(clockTime), getTimeline().getElapsedTimeTillFirstBoundary(clockTime), getAnimationData());
    }

    @Override
    public AnimationState getState(long clockTime) {
        return new AnimationState.ActiveState(animationScript,
                getTimeline().getElapsedTime(clockTime));
    }

    @Override
    public AnimationData getAnimationData() {
        return animationScript.getAnimationData();
    }
}
