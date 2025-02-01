package ru.timeconqueror.timecore.animation;

import lombok.Getter;
import ru.timeconqueror.timecore.animation.action.AnimationEventListener;
import ru.timeconqueror.timecore.animation.network.AnimationState;
import ru.timeconqueror.timecore.animation.watcher.AbstractAnimationTicker;
import ru.timeconqueror.timecore.animation.watcher.AnimationTickerImpl;
import ru.timeconqueror.timecore.animation.watcher.EmptyAnimationTicker;
import ru.timeconqueror.timecore.animation.watcher.TransitionTicker;
import ru.timeconqueror.timecore.api.animation.AnimationScript;
import ru.timeconqueror.timecore.api.animation.BlendType;
import ru.timeconqueror.timecore.api.animation.Layer;
import ru.timeconqueror.timecore.api.animation.builders.LayerDefinition;
import ru.timeconqueror.timecore.api.client.render.model.ITimeModel;
import ru.timeconqueror.timecore.molang.TCMolangRuntime;

import java.util.ArrayList;
import java.util.List;

public class LayerImpl implements Layer, AnimationController {
    @Getter
    private final String name;
    @Getter
    private final BlendType blendType;
    @Getter
    private final float weight;
    @Getter
    private final TCMolangRuntime environment;
    private final List<AnimationEventListener> eventListeners;
    @Getter
    private AbstractAnimationTicker currentTicker = EmptyAnimationTicker.INSTANCE;

    public LayerImpl(BaseAnimationManager animationManager, LayerDefinition layerDefinition) {
        this.name = layerDefinition.name();
        this.blendType = layerDefinition.blendType();
        this.weight = layerDefinition.weight();

        environment = createMolangEnvironment(animationManager);
        this.eventListeners = new ArrayList<>();
    }

    private TCMolangRuntime createMolangEnvironment(BaseAnimationManager animationManager) {
        TCMolangRuntime runtime = new TCMolangRuntime();
        animationManager.getSharedMolangObjects().forEach(runtime::loadLibrary);
        return runtime;
    }

    public void update(long clockTime) {
        while (true) {
            AbstractAnimationTicker currentTicker = getCurrentTicker();
            currentTicker.update(this, clockTime);

            AbstractAnimationTicker newTicker = getCurrentTicker();
            if (newTicker == currentTicker) {
                eventListeners.forEach(listener -> listener.onAnimationTick(this.name, currentTicker, clockTime));
                break;
            }
        }
    }

    public void apply(ITimeModel model, long clockTime) {
        getCurrentTicker().apply(model, getBlendType(), getWeight(), getEnvironment(), clockTime);
    }

    @Override
    public boolean startAnimationScript(AnimationScript animationScript, long clockTime) {
        AnimationData animationData = animationScript.getAnimationData();
        if (animationData.isIgnorable() && getCurrentTicker().canIgnore(animationData)) {
            return false;
        }

        AnimationTickerImpl animationTicker = new AnimationTickerImpl(animationScript, clockTime);
        if (animationData.getTransitionTime() == 0) {
            setCurrentTicker(animationTicker, clockTime);
        } else {
            setCurrentTicker(new TransitionTicker(getCurrentTicker(), animationScript, clockTime, animationData.getTransitionTime()), clockTime);
        }

        return true;
    }

    @Override
    public void removeAnimation(long clockTime, int transitionTime) {
        if (getCurrentTicker().isEmpty()) return;

        if (transitionTime == 0) {
            setCurrentTicker(EmptyAnimationTicker.INSTANCE, clockTime);
            return;
        }

        setCurrentTicker(new TransitionTicker(getCurrentTicker(), null, clockTime, transitionTime), clockTime);
    }

    public void setCurrentTicker(AbstractAnimationTicker ticker, long clockTime) {
        eventListeners.forEach(listener -> listener.onAnimationStopped(this.name, getCurrentTicker(), clockTime));

        this.currentTicker = ticker;

        eventListeners.forEach(listener -> listener.onAnimationStarted(this.name, getCurrentTicker()));
    }

    @Override
    public void addAnimationEventListener(AnimationEventListener listener) {
        this.eventListeners.add(listener);
    }

    @Override
    public void removeAnimationEventListener(AnimationEventListener listener) {
        this.eventListeners.remove(listener);
    }

    public AnimationState getAnimationState(long clockTime) {
        return getCurrentTicker().getState(clockTime);
    }

    public void setAnimationState(AnimationState state, long clockTime) {
        setCurrentTicker(AbstractAnimationTicker.fromState(state, clockTime), clockTime);
    }
}
