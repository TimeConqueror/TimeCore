package ru.timeconqueror.timecore.animation.action;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Nullable;
import ru.timeconqueror.timecore.animation.AnimationCompanionData;
import ru.timeconqueror.timecore.animation.watcher.AnimationTickerImpl;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.animation.AnimationScript;
import ru.timeconqueror.timecore.api.animation.AnimationTicker;
import ru.timeconqueror.timecore.api.animation.action.ActionContext;
import ru.timeconqueror.timecore.api.animation.action.ActionInstance;
import ru.timeconqueror.timecore.api.animation.action.AnimationUpdateListener;
import ru.timeconqueror.timecore.api.util.CollectionUtils;
import ru.timeconqueror.timecore.api.util.Empty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Log4j2
public class LayerActionManager implements AnimationEventListener {
    public static boolean loggerEnabled = true;
    private final AnimatedObject<?> owner;
    private final PredefinedActionManagerImpl<?> predefinedActionManagerImpl;
    @Nullable
    private List<LayerActionManager.ActionTicker> currentActions = null;

    public LayerActionManager(AnimatedObject<?> owner, PredefinedActionManagerImpl<?> predefinedActionManagerImpl) {
        this.owner = owner;
        this.predefinedActionManagerImpl = predefinedActionManagerImpl;
    }

    @Override
    public void onAnimationStarted(String layerName, AnimationTicker ticker) {
        if (ticker instanceof AnimationTickerImpl impl) {
            AnimationScript animationScript = impl.getAnimationScript();
            var companionData = animationScript.getCompanionData();
            if (companionData != AnimationCompanionData.EMPTY) {
                currentActions = new ArrayList<>();

                if (!companionData.getInplaceActions().isEmpty()) {
                    currentActions.addAll(CollectionUtils.mapList(companionData.getInplaceActions(), ActionTicker::new));
                }

                if (!companionData.getPredefinedActionsToPlay().isEmpty()) {
                    companionData.getPredefinedActionsToPlay().stream()
                            .map(predefinedActionManagerImpl::tryCreateAction)
                            .filter(Objects::nonNull)
                            .map(ActionTicker::new)
                            .forEach(actionTicker -> currentActions.add(actionTicker));
                }

                if (loggerEnabled) {
                    log.debug("Added actions on layer '{}': {}", layerName, getCurrentActionIds());
                }
            }
        }
    }

    @Override
    public void onAnimationStopped(String layerName, AnimationTicker ticker) {
        if (ticker instanceof AnimationTickerImpl && currentActions != null) {
            if (loggerEnabled) {
                log.debug("Stopped actions on layer '{}': {}", layerName, getCurrentActionIds());
            }
            currentActions = null;
        }
    }

    @Override
    public void onAnimationTick(String layerName, AnimationTicker ticker, long clockTime) {
        if (ticker instanceof AnimationTickerImpl && currentActions != null) {
            for (ActionTicker currentAction : currentActions) {
                currentAction.onUpdate(ticker, owner, clockTime);
            }
        }
    }

    private List<String> getCurrentActionIds() {
        return currentActions != null ? CollectionUtils.mapList(currentActions, actionTicker -> actionTicker.actionInstance.getId()) : Empty.list();
    }

    public static class ActionTicker {
        private final ActionInstance<?, ?> actionInstance;
        private int lastAnimationCycleIndex = 0;

        public ActionTicker(ActionInstance<?, ?> actionInstance) {
            this.actionInstance = actionInstance;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        public void onUpdate(AnimationTicker ticker, AnimatedObject<?> owner, long clockTime) {
            AnimationUpdateListener listener = actionInstance.getUpdateListener();
            ActionContext ctx = new ActionContext(ticker, owner, actionInstance.getData(), clockTime, lastAnimationCycleIndex);
            lastAnimationCycleIndex = listener.onUpdate(ctx);
        }
    }
}
