package ru.timeconqueror.timecore.animation.action;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.animation.action.AnimationUpdateListener;

import java.util.function.Function;

@SuppressWarnings({"unchecked", "rawtypes"})
@Getter
@Builder
public class ActionFactory<T extends AnimatedObject<T>, DATA> {
    private static final Function SUPPLY_SELF = Function.identity();
    private static final Function SUPPLY_NOTHING = o -> null;

    @Builder.Default
    private final RunOnSide runOnSide = RunOnSide.SERVER;
    @NonNull
    private final AnimationUpdateListener<T, DATA> listener;
    @NonNull
    private final Function<T, DATA> dataSupplier;

    public static <T extends AnimatedObject<T>> Function<T, Void> supplyNothing() {
        return ((Function<T, Void>) SUPPLY_NOTHING);
    }

    public static <T extends AnimatedObject<T>> Function<T, T> supplySelf() {
        return ((Function<T, T>) SUPPLY_SELF);
    }
}