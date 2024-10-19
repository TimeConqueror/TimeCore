package ru.timeconqueror.timecore.animation.watcher;

import gg.moonflower.molangcompiler.api.MolangEnvironment;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import ru.timeconqueror.timecore.animation.AnimationController;
import ru.timeconqueror.timecore.animation.AnimationData;
import ru.timeconqueror.timecore.animation.network.AnimationState;
import ru.timeconqueror.timecore.api.animation.AnimationScript;
import ru.timeconqueror.timecore.api.animation.BlendType;
import ru.timeconqueror.timecore.api.client.render.model.ITimeModel;
import ru.timeconqueror.timecore.api.util.MathUtils;

public class TransitionTicker extends AbstractAnimationTicker {
    @Getter
    private final AbstractAnimationTicker source;
    private final long sourceFreezeTime;
    @Getter
    private final AbstractAnimationTicker destination;

    public TransitionTicker(AbstractAnimationTicker source, @Nullable AnimationScript destinationScript, long startClockTime, int transitionTime) {
        super(new Timeline(transitionTime, 1.0F, false, startClockTime, 0));
        this.source = source;
        this.sourceFreezeTime = startClockTime;
        this.destination = destinationScript == null ? EmptyAnimationTicker.INSTANCE : new AnimationTickerImpl(destinationScript, startClockTime + transitionTime);
    }

    public TransitionTicker(AnimationState.TransitionState transitionState, long currentClockTime) {
        super(new Timeline(transitionState.getTransitionLength(), 1.0F, false, currentClockTime - transitionState.getElapsedTime(), 0));
        this.source = fromState(transitionState.getSource(), currentClockTime);
        this.destination = fromState(transitionState.getDestination(), currentClockTime);
        this.sourceFreezeTime = currentClockTime - transitionState.getSourceTimeInFreeze();
    }

    @Override
    public void apply(ITimeModel model, BlendType blendType, float outerWeight, MolangEnvironment environment, long clockTime) {
        int animationLength = getAnimationLength();
        float transitionProgress = animationLength != 0 ? ((float) getAnimationTimeAt(clockTime)) / animationLength : 1F;
        transitionProgress = MathUtils.coerceInRange(transitionProgress, 0, 1);

        source.apply(model, blendType, outerWeight * (1 - transitionProgress), environment, sourceFreezeTime);
        destination.apply(model, BlendType.ADD, outerWeight * transitionProgress, environment, getTimeline().getStartClockTime() + getAnimationLength());
    }

    @Override
    public void update(AnimationController animationController, long clockTime) {
        if (getAnimationTimeAt(clockTime) < getAnimationLength()) return;

        animationController.setCurrentTicker(destination);
    }

    @Override
    public boolean canIgnore(AnimationData data) {
        return source.canIgnore(data) || destination.canIgnore(data);
    }

    @Override
    public boolean isTransition() {
        return true;
    }

    @Override
    public AnimationData getAnimationData() {
        return destination.getAnimationData();
    }

    @Override
    public int getAnimationTimeAt(long clockTime) {
        return getTimeline().getAnimationTime(clockTime, false);
    }

    @Override
    public String print(long clockTime) {
        return String.format("Transition: Elapsed: %d/%dms, \n\tSource: %s, \n\tDestination: %s", getElapsedTimeAt(clockTime), getAnimationLength(), source.print(clockTime), destination.print(clockTime));
    }

    @Override
    public AnimationState getState(long clockTime) {
        Timeline timeline = getTimeline();
        return new AnimationState.TransitionState(
                source.getState(clockTime),
                destination.getState(clockTime),
                timeline.getLength(),
                timeline.getElapsedTime(clockTime),
                (int) (clockTime - sourceFreezeTime)
        );
    }
}
