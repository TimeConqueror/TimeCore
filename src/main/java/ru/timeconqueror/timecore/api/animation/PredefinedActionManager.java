package ru.timeconqueror.timecore.api.animation;

public interface PredefinedActionManager {
    boolean isKnown(String actionId);

    boolean shouldBeSynced(String actionId);

    boolean canBePlayed(String actionId);
}
