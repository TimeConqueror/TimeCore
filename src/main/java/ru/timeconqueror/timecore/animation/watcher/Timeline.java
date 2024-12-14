package ru.timeconqueror.timecore.animation.watcher;

import lombok.Getter;
import ru.timeconqueror.timecore.api.util.MathUtils;
import ru.timeconqueror.timecore.api.util.Requirements;

/**
 * Represents the animation timeline and is used to calculate the progress of animation. It's immutable. <br>
 * Some notations:<br>
 * 1. Boundary represents the animation edge, where animation is ended.
 * It is located at the 'length' for non-reversed animation on timeline and at zero for reversed one.<br>
 * 2. Cycle index represents the index of the current loop, if the animation is considered to be looped.
 */
@Getter
public class Timeline {
    private final float speed;
    private final int length;
    private final boolean reversed;
    private final long startClockTime;
    private final int animationStartTime;

    public Timeline(int length, float speed, boolean reversed, long startClockTime, int animationStartTime) {
        Requirements.inRangeInclusive(animationStartTime, 0, length);

        this.length = length;
        this.speed = speed;
        this.reversed = reversed;
        this.startClockTime = startClockTime;
        this.animationStartTime = animationStartTime;
    }

    /**
     * Returns the clock time it will take to play the animation, as if it were not looped and taking into account the provided parameters.
     * If speed is 0, it will return {@link Integer#MAX_VALUE}.
     */
    public static int getFirstBoundaryElapsedLength(int length, int animationStartTime, float speed, boolean reversed) {
        if (speed < 0.001) {
            return Integer.MAX_VALUE;
        }

        return (int) Math.ceil(getFirstBoundaryAnimationLength(length, animationStartTime, reversed) / speed);
    }

    public static int getFirstBoundaryAnimationLength(int length, int animationStartTime, boolean reversed) {
        if (reversed) {
            return animationStartTime;
        } else {
            return length - animationStartTime;
        }
    }

    public int getFirstBoundaryAnimationLength() {
        return getFirstBoundaryAnimationLength(length, animationStartTime, reversed);
    }

    /**
     * Returns the remaining clock time it will take to play the animation to get to its first boundary (which is start or end).
     */
    public long getElapsedTimeTillFirstBoundary(long clockTime) {
        long absAnimationTime = getAbsoluteAnimationTime(clockTime);
        int firstBoundaryLength = getFirstBoundaryAnimationLength();

        return Math.max(firstBoundaryLength - absAnimationTime, 0);
    }

    /**
     * Returns the clock time of when the timeline reaches its first boundary
     */
    public long getClockTimeOnFirstBoundary() {
        int elapsedLength = getFirstBoundaryElapsedLength(length, animationStartTime, speed, reversed);
        if (elapsedLength == Integer.MAX_VALUE) {
            return Long.MAX_VALUE;
        } else {
            return startClockTime + elapsedLength;
        }
    }

    /**
     * Represents the progress within the animation length.
     * Returns the progress time, which may vary from 0 to animation length.
     * Being reversed, it goes backwards.
     *
     * @see #getElapsedTime(long)
     */
    public int getAnimationTime(long clockTime, boolean looped) {
        long animationTime = getAbsoluteAnimationTime(clockTime);

        if (reversed) {
            animationTime = animationStartTime - animationTime;
        } else {
            animationTime += animationStartTime;
        }

        if (looped) {
            animationTime = animationTime % (length + 1);
            if (animationTime < 0) {
                animationTime += length + 1;
            }
        }

        return (int) MathUtils.coerceInRange(animationTime, 0, length);
    }

    /**
     * Returns the amount of clock time which is passed from the start of bound animation.
     * May also return negative values.
     */
    public int getElapsedTime(long clockTime) {
        return (int) (clockTime - startClockTime);
    }

    // ###############################################
    // ######       For actions computing       ######
    // ###############################################

    public int getCycleAnimationLength(long cycleIndex) {
        if (cycleIndex == 0) {
            return getFirstBoundaryAnimationLength();
        } else {
            return length;
        }
    }

    public boolean isAnimationTimeReachedOnCurrentCycle(long clockTime, int animationTimeIn) {
        long cycleIndex = getCycleIndex(clockTime);

        // we need to convert absolute animation time, which reflect neither animationStartTime nor reversed
        // to the PASSED animation time
        if (reversed) {
            animationTimeIn = getCycleAnimationLength(cycleIndex) - animationTimeIn;
        } else if (cycleIndex == 0) {
            animationTimeIn = animationTimeIn - animationStartTime;
        }

        int animationTimeOnCycle = getAnimationTimeOnCycleNonReversed(clockTime);

        return animationTimeOnCycle >= animationTimeIn;
    }

    /**
     * Returns the cycle index, starting from zero. The index is calculated for looped animation, which means
     * that if the animation is non-looped, all indexes higher than zero should be considered as the end of the animation.
     */
    public long getCycleIndex(long clockTime) {
        long absAnimationTime = getAbsoluteAnimationTime(clockTime);

        if (length == 0) {
            return absAnimationTime;
        }

        if (absAnimationTime <= getFirstBoundaryAnimationLength()) {
            return 0;
        }

        absAnimationTime = absAnimationTime - getFirstBoundaryAnimationLength();

        long passedCycles = absAnimationTime / length;

        // since zero animation time and max animation time in boundary are the same and cannot be attached to a single cycle
        // all zero animation time are considered as max animation time in previous cycle
        if (absAnimationTime % length != 0) {
            passedCycles++;
        }

        return passedCycles;
    }

    private int getAnimationTimeOnCycleNonReversed(long clockTime) {
        if (length == 0) return 0;

        long absAnimationTime = getAbsoluteAnimationTime(clockTime);

        if (absAnimationTime <= getFirstBoundaryAnimationLength()) {
            return (int) absAnimationTime;
        }

        absAnimationTime = absAnimationTime - getFirstBoundaryAnimationLength();

        // since zero animation time and max animation time in boundary are the same and cannot be attached to a single cycle
        // all zero animation time are considered as max animation time in previous cycle
        if (absAnimationTime % length == 0) {
            return length;
        }

        return (int) (absAnimationTime % length);
    }

    private long getAbsoluteAnimationTime(long clockTime) {
        return Math.round(getElapsedTime(clockTime) * speed);
    }
}
