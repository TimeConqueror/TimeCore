package ru.timeconqueror.timecore.api.animation;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import ru.timeconqueror.timecore.animation.action.BakedActionImpl;
import ru.timeconqueror.timecore.api.animation.action.ActionDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class AnimationBundle<T extends AnimatedObject<T>, PROPS> {
    private final Map<String, ActionDefinition<T, PROPS>> actions = new HashMap<>(0);
    private AnimationStarter starter;
    private String layerName;

    public static <T extends AnimatedObject<T>, PROPS> AnimationBundleBuilder<T, PROPS> builder() {
        return new AnimationBundleBuilder<>();
    }

    public AnimationScript.Builder toScriptBuilder(PROPS props) {
        return AnimationScript.builder(starter)
                .withInplaceActions(
                        getActions().entrySet().stream()
                                .map(e -> new BakedActionImpl<>(e.getKey(),
                                        List.of(new BakedActionImpl.ActionWithProps<>(e.getValue(), props))))
                                .collect(Collectors.toList())
                );
    }

    @Log4j2
    public static class AnimationBundleBuilder<T extends AnimatedObject<T>, PROPS> {
        private final AnimationBundle<T, PROPS> bundle = new AnimationBundle<>();

        public AnimationBundleBuilder<T, PROPS> starter(AnimationStarter starter) {
            bundle.starter = starter;
            return this;
        }

        public AnimationBundleBuilder<T, PROPS> layerName(String layerName) {
            bundle.layerName = layerName;
            return this;
        }

        public AnimationBundleBuilder<T, PROPS> action(String id, ActionDefinition<T, PROPS> actionDefinition) {
            if (bundle.actions.put(id, actionDefinition) != null) {
                log.error("Action with id {} was placed to the action map twice, using the last one...", id);
            }
            return this;
        }

        public AnimationBundle<T, PROPS> build() {
            Objects.requireNonNull(bundle.starter);
            Objects.requireNonNull(bundle.layerName);

            return bundle;
        }
    }
}
