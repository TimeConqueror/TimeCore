package ru.timeconqueror.timecore.api.animation;

import org.jetbrains.annotations.Nullable;
import ru.timeconqueror.timecore.animation.AnimationCompanionData;
import ru.timeconqueror.timecore.animation.AnimationData;
import ru.timeconqueror.timecore.animation.AnimationScriptImpl;
import ru.timeconqueror.timecore.animation.component.LoopMode;
import ru.timeconqueror.timecore.api.animation.action.ActionInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface AnimationScript {
    AnimationData getAnimationData();

    AnimationCompanionData getCompanionData();

    @Nullable
    AnimationScript getNextScript();

    static Builder builder(AnimationStarter starter) {
        return builder(starter.getData());
    }

    static Builder builder(AnimationData animationData) {
        return new Builder(animationData);
    }

    class Builder {
        private final AnimationData animationData;
        @Nullable
        private List<String> predefinedActions;
        @Nullable
        private List<ActionInstance<?, ?>> inplaceActions;
        @Nullable
        private Builder nextScriptBuilder;

        public Builder(AnimationData data) {
            this.animationData = data;
        }

        /**
         * Setting this, you can make a chain of played animations.
         * As soon as one ends, the next one will start immediately.
         * This setting will avoid unpleasant flickering when moving from one animation to another.
         * <br>
         * <b color=yellow>Forces {@link LoopMode#DO_NOT_LOOP} to current script's animation</b>
         * Default: null.
         */
        public Builder withNext(@Nullable Builder nextScriptBuilder) {
            this.nextScriptBuilder = nextScriptBuilder;
            return this;
        }

        public Builder withInplaceAction(ActionInstance<?, ?> action) {
            if (this.inplaceActions == null) {
                this.inplaceActions = new ArrayList<>();
            }
            this.inplaceActions.add(action);
            return this;
        }

        public <T, DATA> Builder withInplaceActions(Collection<ActionInstance<? super T, DATA>> actions) {
            if (this.inplaceActions == null) {
                this.inplaceActions = new ArrayList<>();
            }
            this.inplaceActions.addAll(actions);
            return this;
        }

        public Builder withPredefinedAction(String predefinedActionId) {
            if (this.predefinedActions == null) {
                this.predefinedActions = new ArrayList<>();
            }
            this.predefinedActions.add(predefinedActionId);
            return this;
        }

        public AnimationScript build(PredefinedActionManager predefinedActionManager) {
            AnimationCompanionData companionData = AnimationCompanionData.EMPTY;

            if (predefinedActions != null ||
                    inplaceActions != null) {

                List<String> actionsToSync = Collections.emptyList();
                List<String> actionsToPlay = Collections.emptyList();
                if (predefinedActions != null) {
                    actionsToSync = new ArrayList<>();
                    actionsToPlay = new ArrayList<>();

                    for (String actionId : predefinedActions) {
                        if (!predefinedActionManager.isKnown(actionId)) {
                            throw new IllegalArgumentException("Unknown action name: " + actionId);
                        }

                        if (predefinedActionManager.shouldBeSynced(actionId)) {
                            actionsToSync.add(actionId);
                        }

                        if (predefinedActionManager.canBePlayed(actionId)) {
                            actionsToPlay.add(actionId);
                        }

                    }
                }

                companionData = new AnimationCompanionData(
                        actionsToSync,
                        actionsToPlay,
                        inplaceActions != null ? inplaceActions : Collections.emptyList()
                );
            }

            AnimationScript nextScript = nextScriptBuilder != null ? nextScriptBuilder.build(predefinedActionManager) : null;

            return new AnimationScriptImpl(animationData, companionData, nextScript);
        }
    }
}
