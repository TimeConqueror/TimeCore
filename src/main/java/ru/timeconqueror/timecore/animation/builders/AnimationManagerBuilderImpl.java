package ru.timeconqueror.timecore.animation.builders;

import org.jetbrains.annotations.ApiStatus;
import ru.timeconqueror.timecore.animation.BaseAnimationManager;
import ru.timeconqueror.timecore.animation.ClientAnimationManager;
import ru.timeconqueror.timecore.animation.ServerAnimationManager;
import ru.timeconqueror.timecore.animation.action.LayerActionManager;
import ru.timeconqueror.timecore.animation.action.PredefinedActionManagerImpl;
import ru.timeconqueror.timecore.animation.network.NetworkDispatcherInstance;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.animation.AnimationConstants;
import ru.timeconqueror.timecore.api.animation.BlendType;
import ru.timeconqueror.timecore.api.animation.Clock;
import ru.timeconqueror.timecore.api.animation.builders.AnimationManagerBuilder;
import ru.timeconqueror.timecore.api.animation.builders.LayerDefinition;
import ru.timeconqueror.timecore.api.util.SingleUseBuilder;
import ru.timeconqueror.timecore.molang.SharedMolangObject;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

public class AnimationManagerBuilderImpl extends SingleUseBuilder implements AnimationManagerBuilder {
    private final LinkedHashMap<String, LayerDefinition> layerDefinitions = new LinkedHashMap<>();

    @Override
    public void addLayer(String name, BlendType blendType, float weight) {
        addLayer(new LayerDefinition(name, blendType, weight));
    }

    @Override
    public void addMainLayer() {
        verifyNotUsed();
        addLayer(AnimationConstants.MAIN_LAYER_NAME, BlendType.OVERWRITE, 1);
    }

    @Override
    public void addLayer(LayerDefinition layerDefinition) {
        verifyNotUsed();
        if (layerDefinitions.put(layerDefinition.name(), layerDefinition) != null) {
            throw new IllegalArgumentException("Layer with location " + layerDefinition.name() + " already exist in provided animation manager.");
        }
    }

    @ApiStatus.Internal
    public <T extends AnimatedObject<T>> BaseAnimationManager build(boolean clientSide, Clock clock, SharedMolangObject sharedMolangObject, Supplier<LayerActionManager> actionManagerFactory, NetworkDispatcherInstance<T> networkDispatcherInstance, PredefinedActionManagerImpl<T> predefinedActionManagerImpl) {
        BaseAnimationManager manager;
        if (!clientSide) {
            manager = new ServerAnimationManager<>(clock, actionManagerFactory, sharedMolangObject, networkDispatcherInstance);
        } else {
            manager = new ClientAnimationManager(clock, actionManagerFactory, sharedMolangObject);
        }

        if (layerDefinitions.isEmpty()) {
            addMainLayer();
        }

        manager.init(layerDefinitions);

        setUsed();

        return manager;
    }
}
