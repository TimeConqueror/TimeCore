package ru.timeconqueror.timecore.animation.action;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.animation.action.BakedAction;

import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@Builder
@RequiredArgsConstructor
public class BakedActionFactory<T extends AnimatedObject<T>> {
    @Builder.Default
    private final RunOnSide runOnSide = RunOnSide.SERVER;
    @NonNull
    private final Function<T, BakedAction<T>> actionFactory;

    public static <T extends AnimatedObject<T>> BakedActionFactoryBuilder<T> builder() {
        return new BakedActionFactoryBuilder<T>();
    }

    public static <T extends AnimatedObject<T>> BakedActionFactoryBuilder<T> builder(/*because Java generics can't get info from nowhere */ Class<T> animatedObject) {
        return new BakedActionFactoryBuilder<T>();
    }

    public static class BakedActionFactoryBuilder<T extends AnimatedObject<T>> {
        public BakedActionFactoryBuilder<T> actionFactory(Supplier<BakedAction<T>> factory) {
            this.actionFactory = owner -> factory.get();
            return this;
        }

        public BakedActionFactoryBuilder<T> actionSingleton(BakedAction<T> action) {
            this.actionFactory = owner -> action;
            return this;
        }
    }
}