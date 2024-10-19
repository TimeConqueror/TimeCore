package ru.timeconqueror.timecore.animation.action;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.animation.PredefinedActionManager;
import ru.timeconqueror.timecore.api.animation.action.ActionInstance;
import ru.timeconqueror.timecore.api.animation.action.AnimationUpdateListener;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class PredefinedActionManagerImpl<T extends AnimatedObject<T>> implements PredefinedActionManager {
    private final T owner;
    private final boolean clientSide;
    private final Map<String, ActionFactory<? super T, ?>> factories = new HashMap<>();

    public <DATA> void register(String id, ActionFactory<? super T, DATA> actionFactory) {
        if (clientSide && actionFactory.getRunOnSide() == RunOnSide.SERVER
                || !clientSide && actionFactory.getRunOnSide() == RunOnSide.CLIENT) {
            return;
        }

        factories.put(id, actionFactory);
    }

    @Nullable
    public ActionInstance<T, ?> tryCreateAction(String id) {
        ActionFactory<? super T, ?> actionFactory = factories.get(id);
        if (actionFactory == null) {
            return null;
        }

        AnimationUpdateListener<? super T, Object> listener = (AnimationUpdateListener<? super T, Object>) actionFactory.getListener();
        Object data = actionFactory.getDataSupplier().apply(owner);

        //noinspection RedundantTypeArguments
        return ActionInstance.<T, Object>of(id, listener, data);
    }

    @Override
    public boolean isKnown(String actionId) {
        return factories.containsKey(actionId);
    }

    @Override
    public boolean shouldBeSynced(String id) {
        ActionFactory<? super T, ?> factory = factories.get(id);
        return factory != null && factory.getRunOnSide() != RunOnSide.SERVER;
    }
}
