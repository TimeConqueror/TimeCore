package ru.timeconqueror.timecore.api.animation;

import lombok.Getter;
import ru.timeconqueror.timecore.animation.AnimationSystem;
import ru.timeconqueror.timecore.animation.action.ActionFactory;
import ru.timeconqueror.timecore.animation.action.AnimationEventListener;
import ru.timeconqueror.timecore.animation.action.PredefinedActionManagerImpl;
import ru.timeconqueror.timecore.animation.clock.TickBasedClock;
import ru.timeconqueror.timecore.animation.network.NetworkDispatcherInstance;

@Getter
public class AnimationSystemImpl<T extends AnimatedObject<T>> implements AnimationSystem<T> {
    private final T owner;
    /**
     * Should sync with client or not
     */
    private final boolean clientSide;
    private final AnimationManager animationManager;
    private final NetworkDispatcherInstance<T> networkDispatcher;
    private final Clock clock;
    private final PredefinedAnimationManager<T> predefinedAnimationManager;
    private final PredefinedActionManagerImpl<T> predefinedActionManagerImpl;

    public AnimationSystemImpl(T owner,
                               boolean clientSide,
                               Clock clock,
                               AnimationManager animationManager,
                               NetworkDispatcherInstance<T> networkDispatcher,
                               PredefinedAnimationManager<T> predefinedAnimationManager,
                               PredefinedActionManagerImpl<T> predefinedActionManagerImpl) {
        this.owner = owner;
        this.clientSide = clientSide;
        this.clock = clock;
        this.animationManager = animationManager;
        this.networkDispatcher = networkDispatcher;
        this.predefinedAnimationManager = predefinedAnimationManager;
        this.predefinedActionManagerImpl = predefinedActionManagerImpl;
    }

    public <DATA> boolean startAnimation(AnimationBundle<T, DATA> animationBundle, DATA actionData) {
        AnimationScript.Builder scriptBuilder = animationBundle.toScriptBuilder(actionData);
        return startAnimationScript(scriptBuilder, animationBundle.getLayerName());
    }

    public boolean startAnimation(AnimationStarter animationStarter, String layerName) {
        return startAnimationScript(AnimationScript.builder(animationStarter), layerName);
    }

    public boolean startAnimationScript(AnimationScript.Builder animationScriptBuilder, String layerName) {
        return getAnimationManager().startAnimationScript(animationScriptBuilder.build(predefinedActionManagerImpl), layerName);
    }

    /**
     * Stops animation from the layer with provided name.
     * Default transition time: {@link AnimationConstants#BASIC_TRANSITION_TIME}
     *
     * @param layerName name of layer, where you need to stop animation.
     */
    public void stopAnimation(String layerName) {
        stopAnimation(layerName, AnimationConstants.BASIC_TRANSITION_TIME);//TODO move constant to special configuration object for each system
    }

    /**
     * Stops animation from the layer with provided name.
     *
     * @param layerName      name of layer, where you need to stop animation.
     * @param transitionTime time of transition to the idle state.
     *                       If this value is bigger than 0, then transition will be created, which will smoothly stop current animation.
     */
    public void stopAnimation(String layerName, int transitionTime) {
        transitionTime = Math.max(transitionTime, 0);
        getAnimationManager().stopAnimation(layerName, transitionTime);
    }

    public void addAnimationEventListener(String layerName, AnimationEventListener listener) {
        getAnimationManager().getLayer(layerName).addAnimationEventListener(listener);
    }

    public void removeAnimationEventListener(String layerName, AnimationEventListener listener) {
        getAnimationManager().getLayer(layerName).removeAnimationEventListener(listener);
    }

    public <DATA> void registerPredefinedAction(String id, ActionFactory<T, DATA> actionFactory) {
        getPredefinedActionManagerImpl().register(id, actionFactory);
    }

    @Override
    public void onTick(boolean clientSide) {
        predefinedAnimationManager.onTick(this, owner);

        if (clock instanceof TickBasedClock tickBasedClock) {
            tickBasedClock.tick();
        }

        animationManager.tick();

        if (!clientSide) {
            // simulate ticking
            animationManager.applyAnimations(null, 0);
        }
    }

    @Override
    public PredefinedActionManagerImpl<T> getPredefinedActionManager() {
        return predefinedActionManagerImpl;
    }
}
