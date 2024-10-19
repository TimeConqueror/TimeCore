package ru.timeconqueror.timecore.animation.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import ru.timeconqueror.timecore.animation.action.AnimationEventListener;
import ru.timeconqueror.timecore.api.animation.AnimationTicker;
import ru.timeconqueror.timecore.api.animation.Clock;

@Log4j2
@RequiredArgsConstructor
public class AnimationDebugEventListener implements AnimationEventListener {
    private final Clock clock;
    private final boolean trackUpdates;

    @Override
    public void onAnimationStarted(String layerName, AnimationTicker ticker) {
        log.debug("Started animation on layer '{}': {}", layerName, ticker.print(clock.getMillis()));
    }

    @Override
    public void onAnimationStopped(String layerName, AnimationTicker ticker) {
        log.debug("Stopped animation on layer '{}': {}", layerName, ticker.print(clock.getMillis()));
    }

    @Override
    public void onAnimationTick(String layerName, AnimationTicker ticker, long clockTime) {
        if (trackUpdates) {
            log.debug("Animation ticked on layer '{}': {}", layerName, ticker.print(clockTime));
        }
    }
}
