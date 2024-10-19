package ru.timeconqueror.timecore.api.animation;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import ru.timeconqueror.timecore.api.animation.action.ActionInstance;
import ru.timeconqueror.timecore.api.animation.action.AnimationUpdateListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class AnimationBundle<T extends AnimatedObject<T>, DATA> {
    private final Map<String, AnimationUpdateListener<? super T, DATA>> actions = new HashMap<>(0);
    private AnimationStarter starter;
    private String layerName;

    public static <T extends AnimatedObject<T>, DATA> AnimationBundleBuilder<T, DATA> builder() {
        return new AnimationBundleBuilder<>();
    }

    public AnimationScript.Builder toScriptBuilder(DATA data) {
        return AnimationScript.builder(starter)
                .withInplaceActions(mapActionsToInstances(data));
    }

    public List<ActionInstance<? super T, DATA>> mapActionsToInstances(DATA data) {
        return getActions().entrySet().stream()
                .map(e -> ActionInstance.<T, DATA>of(e.getKey(), e.getValue(), data))
                .collect(Collectors.toList());
    }

    @Log4j2
    public static class AnimationBundleBuilder<T extends AnimatedObject<T>, DATA> {
        private final AnimationBundle<T, DATA> bundle = new AnimationBundle<>();

        public AnimationBundleBuilder<T, DATA> starter(AnimationStarter starter) {
            bundle.starter = starter;
            return this;
        }

        public AnimationBundleBuilder<T, DATA> layerName(String layerName) {
            bundle.layerName = layerName;
            return this;
        }

        public AnimationBundleBuilder<T, DATA> action(String id, AnimationUpdateListener<? super T, DATA> action) {
            if (bundle.actions.put(id, action) != null) {
                log.error("Action with id {} was placed to the action map twice, using the last one...", id);
            }
            return this;
        }

        public AnimationBundle<T, DATA> build() {
            Objects.requireNonNull(bundle.starter);
            Objects.requireNonNull(bundle.layerName);

            return bundle;
        }
    }
}
