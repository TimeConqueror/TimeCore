package ru.timeconqueror.timecore.animation;

import ru.timeconqueror.timecore.animation.action.LayerActionManager;
import ru.timeconqueror.timecore.api.animation.Clock;
import ru.timeconqueror.timecore.molang.SharedMolangObject;

import java.util.function.Supplier;

public class ClientAnimationManager extends BaseAnimationManager {

    public ClientAnimationManager(Clock clock, Supplier<LayerActionManager> actionManagerFactory, SharedMolangObject sharedMolangObject) {
        super(clock, actionManagerFactory, sharedMolangObject);
    }
}
