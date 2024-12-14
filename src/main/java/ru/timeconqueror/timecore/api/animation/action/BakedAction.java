package ru.timeconqueror.timecore.api.animation.action;

import org.jetbrains.annotations.Nullable;
import ru.timeconqueror.timecore.animation.action.BakedActionImpl;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;

import java.util.ArrayList;
import java.util.List;

public interface BakedAction<T extends AnimatedObject<T>> {

    long onUpdate(ActionContext<T> ctx);

    @Nullable
    String getId();

    /**
     * @see #builder(Class)
     */
    static <T extends AnimatedObject<T>> Builder<T> builder() {
        return new Builder<>();
    }

    static <T extends AnimatedObject<T>> Builder<T> builder(/*because Java generics can't get info from nowhere */ Class<T> animatedObject) {
        return builder();
    }

    class Builder<T extends AnimatedObject<T>> {
        private final List<BakedActionImpl.ActionWithProps<T>> actions = new ArrayList<>(1);
        private String id = "undefined";

        public Builder<T> id(String id) {
            this.id = id;
            return this;
        }

        public Builder<T> appendAction(ActionDefinition<T, Void> actionDefinition) {
            actions.add(new BakedActionImpl.ActionWithProps<>(actionDefinition, null));
            return this;
        }

        public <PROPS> Builder<T> appendAction(ActionDefinition<T, PROPS> actionDefinition, PROPS props) {
            actions.add(new BakedActionImpl.ActionWithProps<>(actionDefinition, props));
            return this;
        }

        public BakedAction<T> build() {
            return new BakedActionImpl<>(id, actions);
        }
    }
}
