package ru.timeconqueror.timecore.animation.action;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Nullable;
import ru.timeconqueror.timecore.animation.watcher.Timeline;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.animation.AnimationTicker;
import ru.timeconqueror.timecore.api.animation.action.ActionContext;
import ru.timeconqueror.timecore.api.animation.action.ActionDefinition;
import ru.timeconqueror.timecore.api.animation.action.BakedAction;
import ru.timeconqueror.timecore.api.util.MathUtils;

import java.util.List;

@Log4j2
public class BakedActionImpl<T extends AnimatedObject<T>> implements BakedAction<T> {
    public static boolean loggerEnabled = false;
    private final List<ActionWithProps<T>> actionDefinitions;
    @Getter
    @Nullable
    private final String id;

    private int cursor = 0;

    public BakedActionImpl(@Nullable String id, List<ActionWithProps<T>> actionDefinitions) {
        this.id = id;
        this.actionDefinitions = actionDefinitions;
    }

    @Override
    public long onUpdate(ActionContext<T> ctx) {
        AnimationTicker ticker = ctx.getTicker();
        Timeline timeline = ticker.getTimeline();

        long lastCycleIndex = ctx.getLastAnimationCycleIndex();
        long clockTime = ctx.getClockTime();

        int maxCycleIndex = !ticker.isLooped() ? 0 : -1;
        long currentCycleIndex = timeline.getCycleIndex(clockTime, maxCycleIndex);
        if (loggerEnabled) {
            log.debug("Updating baked action: lastCycleIndex: {}, currentCycleIndex: {}", lastCycleIndex, currentCycleIndex);
        }

        // > - means the action already happened on this cycle
        mainLoop:
        while (lastCycleIndex <= currentCycleIndex) {
            if (lastCycleIndex < currentCycleIndex) {
                for (int i = cursor; i < actionDefinitions.size(); i++) {
                    ActionWithProps<T> action = actionDefinitions.get(cursor);
                    if (loggerEnabled) {
                        log.debug("Running sub-action {} for cycle {}", cursor, lastCycleIndex);
                    }
                    action.onUpdate(ctx);
                }
                cursor = 0;
                lastCycleIndex++;
            }

            if (lastCycleIndex == currentCycleIndex) {
                while (true) {
                    ActionWithProps<T> actionWithProps = actionDefinitions.get(cursor);
                    ActionDefinition<T, ?> actionDefinition = actionWithProps.getActionDefinition();
                    int triggeringAnimationTime = actionDefinition.getTriggeringAnimationTime(ctx);
                    int normalizedTriggeringAnimTime = MathUtils.coerceInRange(triggeringAnimationTime, 0, timeline.getLength());

                    if (!timeline.isAnimationTimeReachedOnCurrentCycle(clockTime, normalizedTriggeringAnimTime)) {
                        break mainLoop;
                    }

                    if (loggerEnabled) {
                        log.debug("Running sub-action {} for cycle {}", cursor, currentCycleIndex);
                    }

                    actionWithProps.onUpdate(ctx);
                    cursor++;

                    if (cursor >= actionDefinitions.size()) {
                        cursor = 0;
                        lastCycleIndex++;
                        break;
                    }
                }
            }
        }

        return lastCycleIndex;
    }

    @Getter
    public static class ActionWithProps<T extends AnimatedObject<T>> {
        private final ActionDefinition<T, ?> actionDefinition;
        private final Object props;

        public <PROPS> ActionWithProps(ActionDefinition<T, PROPS> actionDefinition, PROPS props) {
            this.actionDefinition = actionDefinition;
            this.props = props;
        }

        public void onUpdate(ActionContext<T> ctx) {
            //noinspection unchecked,rawtypes
            ((ActionDefinition) actionDefinition).onUpdate(ctx, props);
        }
    }
}
