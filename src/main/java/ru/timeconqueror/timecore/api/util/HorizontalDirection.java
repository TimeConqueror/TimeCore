package ru.timeconqueror.timecore.api.util;

import net.minecraft.core.Direction;

public enum HorizontalDirection {
    NORTH(Direction.NORTH),
    EAST(Direction.EAST),
    SOUTH(Direction.SOUTH),
    WEST(Direction.WEST);

    private final Direction direction;

    HorizontalDirection(Direction direction) {
        this.direction = direction;
    }

    public Direction toVanilla() {
        return direction;
    }
}
