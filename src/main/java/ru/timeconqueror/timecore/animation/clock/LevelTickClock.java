package ru.timeconqueror.timecore.animation.clock;

import lombok.AllArgsConstructor;
import net.minecraft.world.level.Level;
import ru.timeconqueror.timecore.api.animation.Clock;

import java.util.function.Supplier;

@AllArgsConstructor
public class LevelTickClock implements Clock {
    private final Supplier<Level> levelSup;

    @Override
    public long getMillis() {
        Level level = levelSup.get();
        return level.getGameTime();
    }
}
