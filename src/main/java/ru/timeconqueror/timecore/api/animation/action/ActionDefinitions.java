package ru.timeconqueror.timecore.api.animation.action;

import ru.timeconqueror.timecore.animation.watcher.Timeline;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.animation.AnimationTicker;

import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;

public class ActionDefinitions {
    public static <T extends AnimatedObject<T>, PROPS> ActionDefinition<T, PROPS> onBoundaryStart(BiConsumer<ActionContext<T>, PROPS> action) {
        return everyCycleAtPercents(0, action);
    }

    public static <T extends AnimatedObject<T>, PROPS> ActionDefinition<T, PROPS> onBoundaryEnd(BiConsumer<ActionContext<T>, PROPS> action) {
        return everyCycleAtPercents(1, action);
    }

    public static <T extends AnimatedObject<T>, PROPS> ActionDefinition<T, PROPS> everyCycleAt(int animationTime, BiConsumer<ActionContext<T>, PROPS> action) {
        return everyCycleAt(ctx -> animationTime, action);
    }

    public static <T extends AnimatedObject<T>, PROPS> ActionDefinition<T, PROPS> everyCycleAtPercents(float animationPercents, BiConsumer<ActionContext<T>, PROPS> action) {
        return everyCycleAt(
                ctx -> {
                    AnimationTicker ticker = ctx.getTicker();
                    Timeline timeline = ticker.getTimeline();

                    int triggerAnimationTime = Math.round(timeline.getLength() * animationPercents);
                    return Math.min(triggerAnimationTime, timeline.getFirstBoundaryAnimationLength());
                },
                action
        );
    }

    private static <T extends AnimatedObject<T>, PROPS> ActionDefinition<T, PROPS> everyCycleAt(ToIntFunction<ActionContext<T>> triggerTimeCalcFunc, BiConsumer<ActionContext<T>, PROPS> action) {
        return new ActionDefinition<>() {
            @Override
            public void onUpdate(ActionContext<T> ctx, PROPS props) {
                action.accept(ctx, props);
            }

            @Override
            public int getTriggeringAnimationTime(ActionContext<T> ctx) {
                return triggerTimeCalcFunc.applyAsInt(ctx);
            }
        };
    }
}
