package ru.timeconqueror.timecore.animation.watcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TimelineTest {
    private static final int ZERO_CYCLE = 0;
    private static Stream<Arguments> nonZeroLengthsAndMaybeReversed() {
        return nonZeroLengths().boxed().flatMap(
                length -> allBooleans().map(reversed -> Arguments.of(length, reversed)));
    }

    private static Stream<Arguments> getCycleIndexTest_DifferentStartAnimationTimeAndMaybeReversed() {
        return IntStream.of(250, 500, 750)
                .boxed()
                .flatMap(length -> allBooleans().map(reversed -> Arguments.of(length, reversed)));
    }

    private static IntStream nonZeroLengths() {
        return IntStream.of(1000, 2000);
    }

    private static Stream<Boolean> allBooleans() {
        return Stream.of(false, true);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    public void getElapsedLengthTillFirstBoundary_ShouldHaveSameLengthWhenDefaultValues(int length) {
        assertEquals(length, Timeline.getFirstBoundaryElapsedLength(length, 0, 1, false));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    public void getElapsedLengthTillFirstBoundary_ShouldBeTwiceLongerDueToSpeed(int length) {
        assertEquals(length * 2, Timeline.getFirstBoundaryElapsedLength(length, 0, 0.5F, false));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 2, 4})
    public void getElapsedLengthTillFirstBoundary_ShouldBeTwiceShorterDueToSpeed(int length) {
        assertEquals(length / 2, Timeline.getFirstBoundaryElapsedLength(length, 0, 2F, false));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    public void getElapsedLengthTillFirstBoundary_ShouldBeSameIfJustReversed(int length) {
        assertEquals(length, Timeline.getFirstBoundaryElapsedLength(length, length, 1F, true));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    public void getElapsedLengthTillFirstBoundary_ShouldBeMaxIfSpeedIsZero(int length) {
        assertEquals(Integer.MAX_VALUE, Timeline.getFirstBoundaryElapsedLength(length, 0, 0, false));
        assertEquals(Integer.MAX_VALUE, Timeline.getFirstBoundaryElapsedLength(length, length, 0, false));
        assertEquals(Integer.MAX_VALUE, Timeline.getFirstBoundaryElapsedLength(length, 0, 0, true));
        assertEquals(Integer.MAX_VALUE, Timeline.getFirstBoundaryElapsedLength(length, length, 0, true));
    }

    @Test
    public void getElapsedLengthTillFirstBoundary_ShouldConsiderStartAnimationTime() {
        // not reversed
        assertEquals(1000, Timeline.getFirstBoundaryElapsedLength(1000, 0, 1, false));
        assertEquals(750, Timeline.getFirstBoundaryElapsedLength(1000, 250, 1, false));
        assertEquals(500, Timeline.getFirstBoundaryElapsedLength(1000, 500, 1, false));
        assertEquals(250, Timeline.getFirstBoundaryElapsedLength(1000, 750, 1, false));
        assertEquals(0, Timeline.getFirstBoundaryElapsedLength(1000, 1000, 1, false));

        //reversed (animationTimeStartFrom should represent the time in non-reversed version of animation)
        assertEquals(1000, Timeline.getFirstBoundaryElapsedLength(1000, 1000, 1, true));
        assertEquals(750, Timeline.getFirstBoundaryElapsedLength(1000, 750, 1, true));
        assertEquals(500, Timeline.getFirstBoundaryElapsedLength(1000, 500, 1, true));
        assertEquals(250, Timeline.getFirstBoundaryElapsedLength(1000, 250, 1, true));
        assertEquals(0, Timeline.getFirstBoundaryElapsedLength(1000, 0, 1, true));
    }

    @ParameterizedTest
    @MethodSource("allBooleans")
    public void getElapsedTimeTillFirstBoundaryTest_NoStartTime(boolean reversed) {
        var timeline = new Timeline(1000, 1, reversed, 0, reversed ? 1000 : 0);

        assertEquals(1001, timeline.getElapsedTimeTillFirstBoundary(-1));
        assertEquals(1000, timeline.getElapsedTimeTillFirstBoundary(0));
        assertEquals(999, timeline.getElapsedTimeTillFirstBoundary(1));
        assertEquals(500, timeline.getElapsedTimeTillFirstBoundary(500));
        assertEquals(0, timeline.getElapsedTimeTillFirstBoundary(1000));
        assertEquals(0, timeline.getElapsedTimeTillFirstBoundary(1001));
    }

    @ParameterizedTest
    @MethodSource("allBooleans")
    public void getElapsedTimeTillFirstBoundaryTest_WithStartTimeNonReversed() {
        var timeline = new Timeline(1000, 1, false, 0, 250);

        assertEquals(751, timeline.getElapsedTimeTillFirstBoundary(-1));
        assertEquals(750, timeline.getElapsedTimeTillFirstBoundary(0));
        assertEquals(749, timeline.getElapsedTimeTillFirstBoundary(1));
        assertEquals(250, timeline.getElapsedTimeTillFirstBoundary(500));
        assertEquals(0, timeline.getElapsedTimeTillFirstBoundary(750));
        assertEquals(0, timeline.getElapsedTimeTillFirstBoundary(751));
    }

    @ParameterizedTest
    @MethodSource("allBooleans")
    public void getElapsedTimeTillFirstBoundaryTest_WithStartTimeReversed() {
        var timeline = new Timeline(1000, 1, true, 0, 250);

        assertEquals(251, timeline.getElapsedTimeTillFirstBoundary(-1));
        assertEquals(250, timeline.getElapsedTimeTillFirstBoundary(0));
        assertEquals(249, timeline.getElapsedTimeTillFirstBoundary(1));
        assertEquals(100, timeline.getElapsedTimeTillFirstBoundary(150));
        assertEquals(0, timeline.getElapsedTimeTillFirstBoundary(250));
        assertEquals(0, timeline.getElapsedTimeTillFirstBoundary(251));
    }

    @Test
    public void getElapsedTimeTest() {
        var timeline = new Timeline(1000, 1, false, 0, 0);
        assertEquals(0, timeline.getElapsedTime(0));
        assertEquals(1000, timeline.getElapsedTime(1000));
        assertEquals(2000, timeline.getElapsedTime(2000));

        timeline = new Timeline(1000, 1, false, 1000, 0);
        assertEquals(-1000, timeline.getElapsedTime(0));
        assertEquals(0, timeline.getElapsedTime(1000));
        assertEquals(1000, timeline.getElapsedTime(2000));
        assertEquals(2000, timeline.getElapsedTime(3000));
    }

    @Test
    public void getAnimationTime_ShouldBeInBoundsAndRightForDefaultValues() {
        var timeline = new Timeline(1000, 1, false, 0, 0);

        assertEquals(0, timeline.getAnimationTime(-1, false));
        assertEquals(0, timeline.getAnimationTime(0, false));
        assertEquals(250, timeline.getAnimationTime(250, false));
        assertEquals(500, timeline.getAnimationTime(500, false));
        assertEquals(750, timeline.getAnimationTime(750, false));
        assertEquals(1000, timeline.getAnimationTime(1000, false));
        assertEquals(1000, timeline.getAnimationTime(1001, false));
        assertEquals(1000, timeline.getAnimationTime(Integer.MAX_VALUE, false));
    }

    @Test
    public void getAnimationTime_ShouldBeRightForVariousSpeed() {
        int length = 1000;
        // stay in start if speed is zero
        var timeline = new Timeline(length, 0, false, 0, 0);
        assertEquals(0, timeline.getAnimationTime(0, false));
        assertEquals(0, timeline.getAnimationTime(500, false));
        assertEquals(0, timeline.getAnimationTime(1000, false));

        // twice shorter
        timeline = new Timeline(length, 0.5F, false, 0, 0);
        assertEquals(0, timeline.getAnimationTime(0, false));
        assertEquals(500, timeline.getAnimationTime(1000, false));
        assertEquals(1000, timeline.getAnimationTime(2000, false));

        // normal speed
        timeline = new Timeline(length, 1, false, 0, 0);
        assertEquals(0, timeline.getAnimationTime(0, false));
        assertEquals(500, timeline.getAnimationTime(500, false));
        assertEquals(1000, timeline.getAnimationTime(1000, false));

        // twice faster
        timeline = new Timeline(length, 2, false, 0, 0);
        assertEquals(0, timeline.getAnimationTime(0, false));
        assertEquals(500, timeline.getAnimationTime(250, false));
        assertEquals(1000, timeline.getAnimationTime(500, false));

        // #### REVERSED variants ####

        // stay in end if speed is zero
        timeline = new Timeline(length, 0, true, 0, length);
        assertEquals(1000, timeline.getAnimationTime(0, false));
        assertEquals(1000, timeline.getAnimationTime(500, false));
        assertEquals(1000, timeline.getAnimationTime(1000, false));

        // twice shorter
        timeline = new Timeline(length, 0.5F, true, 0, length);
        assertEquals(1000, timeline.getAnimationTime(0, false));
        assertEquals(500, timeline.getAnimationTime(1000, false));
        assertEquals(0, timeline.getAnimationTime(2000, false));

        // normal speed
        timeline = new Timeline(length, 1, true, 0, length);
        assertEquals(1000, timeline.getAnimationTime(0, false));
        assertEquals(500, timeline.getAnimationTime(500, false));
        assertEquals(0, timeline.getAnimationTime(1000, false));

        // twice faster
        timeline = new Timeline(length, 2, true, 0, length);
        assertEquals(1000, timeline.getAnimationTime(0, false));
        assertEquals(500, timeline.getAnimationTime(250, false));
        assertEquals(0, timeline.getAnimationTime(500, false));
    }

    @ParameterizedTest
    @MethodSource("allBooleans")
    public void getAnimationTime_ShouldBeRightForVariousAnimationStartTime(boolean looped /*should not have any influence*/) {
        // from middle to end boundary
        var timeline = new Timeline(1000, 1, false, 0, 500);
        assertEquals(500, timeline.getAnimationTime(0, looped));
        assertEquals(750, timeline.getAnimationTime(250, looped));
        assertEquals(1000, timeline.getAnimationTime(500, looped));

        // from last quarter to end boundary
        timeline = new Timeline(1000, 1, false, 0, 750);
        assertEquals(750, timeline.getAnimationTime(0, looped));
        assertEquals(750 + 125, timeline.getAnimationTime(125, looped));
        assertEquals(750 + 250, timeline.getAnimationTime(250, looped));

        // from middle to start boundary
        timeline = new Timeline(1000, 1, true, 0, 500);
        assertEquals(500, timeline.getAnimationTime(0, looped));
        assertEquals(250, timeline.getAnimationTime(250, looped));
        assertEquals(0, timeline.getAnimationTime(500, looped));

        // from last quarter to start boundary
        timeline = new Timeline(1000, 1, true, 0, 750);
        assertEquals(750, timeline.getAnimationTime(0, looped));
        assertEquals(250, timeline.getAnimationTime(500, looped));
        assertEquals(0, timeline.getAnimationTime(750, looped));
    }

    @Test
    public void getAnimationTime_ShouldStayIfNotLoopedAndStartedInTheEnd() {
        // stay at end
        var timeline = new Timeline(1000, 1, false, 0, 1000);
        assertEquals(1000, timeline.getAnimationTime(0, false));
        assertEquals(1000, timeline.getAnimationTime(500, false));
        assertEquals(1000, timeline.getAnimationTime(1000, false));

        // stay at start
        timeline = new Timeline(1000, 1, true, 0, 0);
        assertEquals(0, timeline.getAnimationTime(0, false));
        assertEquals(0, timeline.getAnimationTime(500, false));
        assertEquals(0, timeline.getAnimationTime(1000, false));
    }

    @Test
    public void getAnimationTime_TestLooped() {
        var length = 1000;

        // default values
        var timeline = new Timeline(length, 1, false, 0, 0);
        assertEquals(length, timeline.getAnimationTime(length, true));
        assertEquals(250, timeline.getAnimationTime(length + 1 + 250, true));
        assertEquals(250, timeline.getAnimationTime((length + 1) * 2 + 250, true));

        // reversed
        timeline = new Timeline(length, 1, true, 0, length);
        assertEquals(0, timeline.getAnimationTime(length, true));
        assertEquals(length, timeline.getAnimationTime(length + 1, true));
        assertEquals(750, timeline.getAnimationTime(length + 1 + 250, true));
        assertEquals(750, timeline.getAnimationTime((length + 1) * 2 + 250, true));

        // from last quarter to end
        timeline = new Timeline(length, 1, false, 0, 750);
        assertEquals(length, timeline.getAnimationTime(250, true));
        assertEquals(0, timeline.getAnimationTime(250 + 1, true));
        assertEquals(250, timeline.getAnimationTime(250 + 1 + 250, true));
        assertEquals(250, timeline.getAnimationTime((250 + 1) + (length + 1) + 250, true));

        // from last quarter to start
        timeline = new Timeline(1000, 1, true, 0, 750);
        assertEquals(0, timeline.getAnimationTime(750, true));
        assertEquals(length, timeline.getAnimationTime(750 + 1, true));
        assertEquals(750, timeline.getAnimationTime(750 + 1 + 250, true));
        assertEquals(750, timeline.getAnimationTime((750 + 1) + (length + 1) + 250, true));
    }

    @ParameterizedTest
    @MethodSource("allBooleans")
    public void getCycleIndex_ZeroLength(boolean reversed) {
        var timeline = new Timeline(0, 1, reversed, 0, 0);
        assertEquals(0, timeline.getCycleIndex(0));
        assertEquals(1, timeline.getCycleIndex(1));
        assertEquals(2, timeline.getCycleIndex(2));
    }

    @ParameterizedTest
    @MethodSource("nonZeroLengthsAndMaybeReversed")
    public void getCycleIndex_NoStartTime(int length, boolean reversed) {
        var timeline = new Timeline(length, 1, reversed, 0, reversed ? length : 0);
        assertEquals(0, timeline.getCycleIndex(0));
        assertEquals(0, timeline.getCycleIndex(length - 1));
        assertEquals(0, timeline.getCycleIndex(length));
        // animTime=0 on cycle 1 if reversed=false & animTime=length on cycle 1 if reversed=true
        assertEquals(1, timeline.getCycleIndex(length + 1));
        // animTime=length on cycle 1 if reversed=false & animTime=0 on cycle 1 if reversed=true
        assertEquals(1, timeline.getCycleIndex(length * 2L - 1));
        assertEquals(1, timeline.getCycleIndex(length * 2L));
        // animTime=0 on cycle 2 if reversed=false & animTime=length on cycle 1 if reversed=true
        assertEquals(2, timeline.getCycleIndex(length * 2L + 1));
        assertEquals(2, timeline.getCycleIndex(length * 3L - 1));
        assertEquals(2, timeline.getCycleIndex(length * 3L));
    }

    @ParameterizedTest
    @MethodSource("getCycleIndexTest_DifferentStartAnimationTimeAndMaybeReversed")
    public void getCycleIndex_NonReversedWithStartAnimationTime(int animationStartTime) {
        int length = 1000;

        var timeline = new Timeline(length, 1, false, 0, animationStartTime);
        assertEquals(0, timeline.getCycleIndex(0));
        assertEquals(0, timeline.getCycleIndex(length - animationStartTime));

        assertEquals(1, timeline.getCycleIndex(length - animationStartTime + 1));
        assertEquals(1, timeline.getCycleIndex((length - animationStartTime) + length));
        assertEquals(2, timeline.getCycleIndex((length - animationStartTime) + length + 1));
    }

    @ParameterizedTest
    @MethodSource("getCycleIndexTest_DifferentStartAnimationTimeAndMaybeReversed")
    public void getCycleIndex_ReversedWithStartAnimationTime(int animationStartTime) {
        int length = 1000;

        var timeline = new Timeline(length, 1, true, 0, animationStartTime);
        assertEquals(0, timeline.getCycleIndex(0));
        assertEquals(0, timeline.getCycleIndex(animationStartTime));

        assertEquals(1, timeline.getCycleIndex(animationStartTime + 1));
        assertEquals(1, timeline.getCycleIndex(animationStartTime + length));
        assertEquals(2, timeline.getCycleIndex(animationStartTime + length + 1));
    }

    private static final int INFINITE_CYCLES = -1;

    @ParameterizedTest
    @MethodSource("allBooleans")
    public void isAnimationTimeReached_TestZeroLength(boolean reversed) {
        var timeline = new Timeline(0, 1, reversed, 0, 0);
        // cycle 0
        assertTrue(timeline.isAnimationTimeReached(0, 0, ZERO_CYCLE));
        // cycle 1
        assertTrue(timeline.isAnimationTimeReached(1, 0, 1));
    }

    @Test
    public void isAnimationTimeReachedWithDefaults() {
        var timeline = new Timeline(1000, 1, false, 0, 0);
        // if the time is at the start boundary
        assertTrue(timeline.isAnimationTimeReached(0, 0, ZERO_CYCLE));
        assertFalse(timeline.isAnimationTimeReached(0, 500, ZERO_CYCLE));
        assertFalse(timeline.isAnimationTimeReached(0, 1000, ZERO_CYCLE));

        // if the time is at the middle
        assertTrue(timeline.isAnimationTimeReached(500, 0, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(500, 500, ZERO_CYCLE));
        assertFalse(timeline.isAnimationTimeReached(500, 1000, ZERO_CYCLE));

        // if the time is at the end boundary
        assertTrue(timeline.isAnimationTimeReached(1000, 0, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(1000, 500, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(1000, 1000, ZERO_CYCLE));

        // check offsets
        assertFalse(timeline.isAnimationTimeReached(499, 500, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(500, 500, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(501, 500, ZERO_CYCLE));
    }

    @Test
    public void isAnimationTimeReachedReversed() {

        var timeline = new Timeline(1000, 1, true, 0, 1000);
        // if the time is at the end boundary
        assertTrue(timeline.isAnimationTimeReached(0, 1000, ZERO_CYCLE));
        assertFalse(timeline.isAnimationTimeReached(0, 500, ZERO_CYCLE));
        assertFalse(timeline.isAnimationTimeReached(0, 0, ZERO_CYCLE));

        // if the time is at the middle
        assertTrue(timeline.isAnimationTimeReached(500, 1000, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(500, 500, ZERO_CYCLE));
        assertFalse(timeline.isAnimationTimeReached(500, 0, ZERO_CYCLE));

        // if the time is at the start boundary
        assertTrue(timeline.isAnimationTimeReached(1000, 1000, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(1000, 500, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(1000, 0, ZERO_CYCLE));

        // check offsets
        assertFalse(timeline.isAnimationTimeReached(499, 500, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(500, 500, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(501, 500, ZERO_CYCLE));
    }

    @Test
    public void isAnimationTimeReached_CheckBoundariesNotReversed() {
        var timeline = new Timeline(1000, 1, false, 0, 0);

        assertTrue(timeline.isAnimationTimeReached(1000, 500, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(1000, 1000, ZERO_CYCLE));

        assertTrue(timeline.isAnimationTimeReached(1001, 0, 1));
        assertTrue(timeline.isAnimationTimeReached(1001, 1, 1));
        assertFalse(timeline.isAnimationTimeReached(1001, 2, 1));
        assertFalse(timeline.isAnimationTimeReached(1001, 1000, 1));

        assertTrue(timeline.isAnimationTimeReached(1500, 0, 1));
        assertTrue(timeline.isAnimationTimeReached(1500, 500, 1));
        assertFalse(timeline.isAnimationTimeReached(1500, 501, 1));
        assertFalse(timeline.isAnimationTimeReached(1500, 1000, 1));

        assertTrue(timeline.isAnimationTimeReached(2000, 500, 1));
        assertTrue(timeline.isAnimationTimeReached(2000, 1000, 1));

        assertTrue(timeline.isAnimationTimeReached(2001, 0, 2));
        assertTrue(timeline.isAnimationTimeReached(2001, 1, 2));
        assertFalse(timeline.isAnimationTimeReached(2001, 2, 2));
        assertFalse(timeline.isAnimationTimeReached(2001, 1000, 2));

        assertTrue(timeline.isAnimationTimeReached(2500, 0, 2));
        assertTrue(timeline.isAnimationTimeReached(2500, 500, 2));
        assertFalse(timeline.isAnimationTimeReached(2500, 501, 2));
        assertFalse(timeline.isAnimationTimeReached(2500, 1000, 2));
    }

    @Test
    public void isAnimationTimeReached_CheckBoundariesReversed() {
        var timeline = new Timeline(1000, 1, true, 0, 1000);

        assertTrue(timeline.isAnimationTimeReached(1000, 500, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(1000, 0, ZERO_CYCLE));

        assertTrue(timeline.isAnimationTimeReached(1001, 1000, 1));
        assertTrue(timeline.isAnimationTimeReached(1001, 999, 1));
        assertFalse(timeline.isAnimationTimeReached(1001, 0, 1));

        assertTrue(timeline.isAnimationTimeReached(1500, 1000, 1));
        assertTrue(timeline.isAnimationTimeReached(1500, 500, 1));
        assertFalse(timeline.isAnimationTimeReached(1500, 499, 1));
        assertFalse(timeline.isAnimationTimeReached(1500, 0, 1));

        assertTrue(timeline.isAnimationTimeReached(2000, 500, 1));
        assertTrue(timeline.isAnimationTimeReached(2000, 0, 1));

        assertTrue(timeline.isAnimationTimeReached(2001, 1000, 2));
        assertTrue(timeline.isAnimationTimeReached(2001, 999, 2));
        assertFalse(timeline.isAnimationTimeReached(2001, 0, 2));

        assertTrue(timeline.isAnimationTimeReached(2500, 1000, 2));
        assertTrue(timeline.isAnimationTimeReached(2500, 500, 2));
        assertFalse(timeline.isAnimationTimeReached(2500, 499, 2));
        assertFalse(timeline.isAnimationTimeReached(2500, 0, 2));
    }

    @Test
    public void isAnimationTimeReached_CheckBoundariesWithStartTime() {
        var timeline = new Timeline(1000, 1, false, 0, 250);

        // end of cycle 0
        assertTrue(timeline.isAnimationTimeReached(750, 0, 0));
        assertTrue(timeline.isAnimationTimeReached(750, 250, 0));
        assertTrue(timeline.isAnimationTimeReached(750, 1000, 0));

        // start of cycle 1
        assertTrue(timeline.isAnimationTimeReached(751, 0, 1));
        assertTrue(timeline.isAnimationTimeReached(751, 1, 1));
        assertFalse(timeline.isAnimationTimeReached(751, 250, 1));
        assertFalse(timeline.isAnimationTimeReached(751, 1000, 1));

        // end of cycle 1
        assertTrue(timeline.isAnimationTimeReached(750 + 1000, 0, 1));
        assertTrue(timeline.isAnimationTimeReached(750 + 1000, 250, 1));
        assertTrue(timeline.isAnimationTimeReached(750 + 1000, 1000, 1));

        // start of cycle 2
        assertTrue(timeline.isAnimationTimeReached(750 + 1000 + 1, 0, 2));
        assertTrue(timeline.isAnimationTimeReached(750 + 1000 + 1, 1, 2));
        assertFalse(timeline.isAnimationTimeReached(750 + 1000 + 1, 2, 2));
        assertFalse(timeline.isAnimationTimeReached(750 + 1000 + 1, 500, 2));
        assertFalse(timeline.isAnimationTimeReached(750 + 1000 + 1, 1000, 2));
    }

    @Test
    public void isAnimationTimeReached_CheckBoundariesWithStartTimeReversed() {
        var timeline = new Timeline(1000, 1, true, 0, 250);

        // end of cycle 0
        assertTrue(timeline.isAnimationTimeReached(250, 1000, 0));
        assertTrue(timeline.isAnimationTimeReached(250, 250, 0));
        assertTrue(timeline.isAnimationTimeReached(250, 0, 0));

        // start of cycle 1
        assertTrue(timeline.isAnimationTimeReached(251, 1000, 1));
        assertTrue(timeline.isAnimationTimeReached(251, 999, 1));
        assertFalse(timeline.isAnimationTimeReached(251, 998, 1));
        assertFalse(timeline.isAnimationTimeReached(251, 0, 1));

        // end of cycle 1
        assertTrue(timeline.isAnimationTimeReached(250 + 1000, 1000, 1));
        assertTrue(timeline.isAnimationTimeReached(250 + 1000, 250, 1));
        assertTrue(timeline.isAnimationTimeReached(250 + 1000, 0, 1));

        // start of cycle 2
        assertTrue(timeline.isAnimationTimeReached(250 + 1000 + 1, 1000, 2));
        assertTrue(timeline.isAnimationTimeReached(250 + 1000 + 1, 999, 2));
        assertFalse(timeline.isAnimationTimeReached(250 + 1000 + 1, 998, 2));
        assertFalse(timeline.isAnimationTimeReached(250 + 1000 + 1, 500, 2));
        assertFalse(timeline.isAnimationTimeReached(250 + 1000 + 1, 0, 2));
    }

    @Test
    public void isAnimationTimeReached_NotReversedWithStartTime() {
        var timeline = new Timeline(1000, 1, false, 0, 250);

        // if the time is at the startTime
        assertTrue(timeline.isAnimationTimeReached(0, 250, ZERO_CYCLE));
        assertFalse(timeline.isAnimationTimeReached(0, 500, ZERO_CYCLE));
        assertFalse(timeline.isAnimationTimeReached(0, 1000, ZERO_CYCLE));

        // if the time is at the middle of animation
        assertTrue(timeline.isAnimationTimeReached(250, 250, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(250, 500, ZERO_CYCLE));
        assertFalse(timeline.isAnimationTimeReached(250, 1000, ZERO_CYCLE));

        // if the time is at the end boundary
        assertTrue(timeline.isAnimationTimeReached(750, 250, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(750, 500, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(750, 1000, ZERO_CYCLE));

        // for animationTime before the startTime (should be considered as always reached)
        assertTrue(timeline.isAnimationTimeReached(0, 0, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(0, 100, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(0, 249, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(750, 0, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(750, 100, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(750, 249, ZERO_CYCLE));

        // for animationTime greater than boundary length (it is considered to never be reached)
        assertFalse(timeline.isAnimationTimeReached(0, 1001, ZERO_CYCLE));
        assertFalse(timeline.isAnimationTimeReached(750, 1001, ZERO_CYCLE));

        // check precision at startTime
        assertTrue(timeline.isAnimationTimeReached(250, 499, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(250, 500, ZERO_CYCLE));
        assertFalse(timeline.isAnimationTimeReached(250, 501, ZERO_CYCLE));
    }

    @Test
    public void isAnimationTimeReachedOnCurrentCycle_ReversedWithStartTime() {
        var timeline = new Timeline(1000, 1, true, 0, 250);

        // if the time is at the startTime
        assertTrue(timeline.isAnimationTimeReached(0, 250, ZERO_CYCLE));
        assertFalse(timeline.isAnimationTimeReached(0, 100, ZERO_CYCLE));
        assertFalse(timeline.isAnimationTimeReached(0, 0, ZERO_CYCLE));

        // if the time is at the middle of animation
        assertTrue(timeline.isAnimationTimeReached(150, 250, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(150, 100, ZERO_CYCLE));
        assertFalse(timeline.isAnimationTimeReached(150, 0, ZERO_CYCLE));

        // if the time is at the end boundary
        assertTrue(timeline.isAnimationTimeReached(250, 250, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(250, 100, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(250, 0, ZERO_CYCLE));

        // for animationTime before the startTime (should be considered as always reached)
        assertTrue(timeline.isAnimationTimeReached(0, 1000, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(0, 500, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(0, 251, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(250, 1000, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(250, 500, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(250, 251, ZERO_CYCLE));

        // for animationTime greater than boundary length (it is considered to never be reached)
        assertFalse(timeline.isAnimationTimeReached(0, -1, ZERO_CYCLE));
        assertFalse(timeline.isAnimationTimeReached(0, -1, ZERO_CYCLE));
        assertFalse(timeline.isAnimationTimeReached(250, -1000, ZERO_CYCLE));
        assertFalse(timeline.isAnimationTimeReached(250, -1000, ZERO_CYCLE));

        // check precision at startTime
        assertTrue(timeline.isAnimationTimeReached(150, 101, ZERO_CYCLE));
        assertTrue(timeline.isAnimationTimeReached(150, 100, ZERO_CYCLE));
        assertFalse(timeline.isAnimationTimeReached(150, 99, ZERO_CYCLE));
    }

    @ParameterizedTest
    @CsvSource({
            "1000, 1, 1000",
            "1000, 2, 500",
            "1000, 5, 200"
    })
    public void getClockTimeOnFirstBoundary_Tests(int length, int speed, int expectedClockTime) {
        Timeline timeline = new Timeline(length, speed, false, 0, 0);
        assertEquals(expectedClockTime, timeline.getClockTimeOnFirstBoundary());
    }

    @Test
    void getAnimationTimeOnCycleNonReversed_testAnimationTimeOnZeroCycle() {
        Timeline timeline = new Timeline(10, 1.0f, false, 0L, 0);

        // absAnimationTime <= firstBoundaryAnimationLength
        assertEquals(5, timeline.getAnimationTimeOnCycleNonReversed(5, 0));
        assertEquals(10, timeline.getAnimationTimeOnCycleNonReversed(10, 0));

        // absAnimationTime > firstBoundaryAnimationLength, out of range should clamp at length
        assertEquals(10, timeline.getAnimationTimeOnCycleNonReversed(15, 0));
    }

    @Test
    void getAnimationTimeOnCycleNonReversed_testAnimationTimeOnSubsequentCycles() {
        Timeline timeline = new Timeline(10, 1.0f, false, 0L, 0);

        // Second cycle
        assertEquals(5, timeline.getAnimationTimeOnCycleNonReversed(15, 1));
        assertEquals(10, timeline.getAnimationTimeOnCycleNonReversed(20, 1));

        // Third cycle
        assertEquals(5, timeline.getAnimationTimeOnCycleNonReversed(25, 2));
        assertEquals(10, timeline.getAnimationTimeOnCycleNonReversed(30, 2));
    }

    @Test
    void getAnimationTimeOnCycleNonReversed_testAnimationTimeWithZeroLength() {
        Timeline timeline = new Timeline(0, 1.0f, false, 0L, 0);

        // When length == 0, should always return 0
        assertEquals(0, timeline.getAnimationTimeOnCycleNonReversed(0L, ZERO_CYCLE));
        assertEquals(0, timeline.getAnimationTimeOnCycleNonReversed(0L, 1));
        assertEquals(0, timeline.getAnimationTimeOnCycleNonReversed(10000L, ZERO_CYCLE));
        assertEquals(0, timeline.getAnimationTimeOnCycleNonReversed(10000L, 1));
    }

    @Test
    void getAnimationTimeOnCycleNonReversed_testAnimationTimeWithHighSpeed() {
        Timeline timeline = new Timeline(10, 2.0f, false, 0L, 0);

        // Double speed, so the absAnimationTime grows faster
        assertEquals(8, timeline.getAnimationTimeOnCycleNonReversed(4, 0));
        assertEquals(10, timeline.getAnimationTimeOnCycleNonReversed(5, 0));
        assertEquals(2, timeline.getAnimationTimeOnCycleNonReversed(6, 1));
    }

    @Test
    void testAnimationTimeOnBoundaryConditions() {
        Timeline timeline = new Timeline(10, 1.0f, false, 0L, 0);

        // Test edge of first boundary
        assertEquals(10, timeline.getAnimationTimeOnCycleNonReversed(10, 0));

        // Test exact cycle boundary
        assertEquals(10, timeline.getAnimationTimeOnCycleNonReversed(20, 1));
        assertEquals(10, timeline.getAnimationTimeOnCycleNonReversed(30, 2));

        // Test just before and after cycle boundaries
        assertEquals(9, timeline.getAnimationTimeOnCycleNonReversed(19, 1));
        assertEquals(1, timeline.getAnimationTimeOnCycleNonReversed(21, 2));
    }
}
