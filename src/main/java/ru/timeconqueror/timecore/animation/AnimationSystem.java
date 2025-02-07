package ru.timeconqueror.timecore.animation;

import ru.timeconqueror.timecore.animation.action.AnimationEventListener;
import ru.timeconqueror.timecore.animation.action.BakedActionFactory;
import ru.timeconqueror.timecore.animation.network.NetworkDispatcherInstance;
import ru.timeconqueror.timecore.api.animation.*;

public interface AnimationSystem<T extends AnimatedObject<T>> {

    boolean startAnimation(AnimationStarter animationStarter, String layerName);

    <DATA> boolean startAnimation(AnimationBundle<T, DATA> animationBundle, DATA actionData);

    boolean startAnimationScript(AnimationScript.Builder animationScriptBuilder, String layerName);

    /**
     * Stops animation from the layer with provided name.
     * Default transition time: {@link AnimationConstants#BASIC_TRANSITION_TIME}
     *
     * @param layerName name of layer, where you need to stop animation.
     */
    void stopAnimation(String layerName);

    /**
     * Stops animation from the layer with provided name.
     *
     * @param layerName      name of layer, where you need to stop animation.
     * @param transitionTime time of transition to the idle state.
     *                       If this value is bigger than 0, then transition will be created, which will smoothly stop current animation.
     */
    void stopAnimation(String layerName, int transitionTime);

    void addAnimationEventListener(String layerName, AnimationEventListener listener);

    void removeAnimationEventListener(String layerName, AnimationEventListener listener);

    void registerPredefinedAction(String id, BakedActionFactory<T> bakedActionFactory);

    void onTick(boolean clientSide);

    /**
     * GETTERS
     */

    boolean isClientSide();

    T getOwner();

    AnimationManager getAnimationManager();

    NetworkDispatcherInstance<T> getNetworkDispatcher();

    Clock getClock();

    PredefinedAnimationManager<T> getPredefinedAnimationManager();

    PredefinedActionManager getPredefinedActionManager();
}
