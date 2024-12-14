package ru.timeconqueror.timecore.api.animation.action;

import ru.timeconqueror.timecore.api.animation.AnimatedObject;

/**
 * @see ActionDefinitions
 */
public interface ActionDefinition<T extends AnimatedObject<T>, PROPS> {
    /**
     * Called on server every tick and one extra time upon animation end until this method returns true.
     * Here you can fully control the behavior of attached animated object.
     */
    void onUpdate(ActionContext<T> ctx, PROPS props);

    int getTriggeringAnimationTime(ActionContext<T> ctx);
}