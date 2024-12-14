package ru.timeconqueror.timecore.animation.action;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.animation.PredefinedActionManager;
import ru.timeconqueror.timecore.api.animation.action.BakedAction;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class PredefinedActionManagerImpl<T extends AnimatedObject<T>> implements PredefinedActionManager {
    private final T owner;
    private final boolean clientSide;
    private final Map<String, BakedActionFactory<? super T>> factories = new HashMap<>();

    public void register(String id, BakedActionFactory<? super T> bakedActionFactory) {
        if (clientSide && bakedActionFactory.getRunOnSide() == RunOnSide.SERVER) {
            return;
        }

        factories.put(id, bakedActionFactory);
    }

    @Nullable
    public BakedAction<T> tryCreateAction(String id) {
        BakedActionFactory<? super T> bakedActionFactory = factories.get(id);
        if (bakedActionFactory == null) {
            return null;
        }

        //noinspection unchecked
        return (BakedAction<T>) bakedActionFactory.getActionFactory().apply(owner);
    }

    @Override
    public boolean isKnown(String actionId) {
        return factories.containsKey(actionId);
    }

    @Override
    public boolean shouldBeSynced(String id) {
        BakedActionFactory<? super T> factory = factories.get(id);
        return factory != null && factory.getRunOnSide() != RunOnSide.SERVER;
    }

    public boolean canBePlayed(String id) {
        RunOnSide side = factories.get(id).getRunOnSide();

        if (clientSide) {
            return side == RunOnSide.BOTH || side == RunOnSide.CLIENT;
        } else {
            return side == RunOnSide.BOTH || side == RunOnSide.SERVER;
        }
    }
}
