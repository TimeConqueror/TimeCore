package ru.timeconqueror.timecore.api.animation.action;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ActionInstance<T extends AnimatedObject<T>, DATA> {
    private final String id;
    private final AnimationUpdateListener<? super T, DATA> updateListener;
    private final DATA data;

    public static <T extends AnimatedObject<T>, DATA> ActionInstance<T, DATA> of(String id, AnimationUpdateListener<? super T, DATA> action, DATA data) {
        //noinspection Convert2Diamond
        return new ActionInstance<T, DATA>(id, action, data);
    }
}
