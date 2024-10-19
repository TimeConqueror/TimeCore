package ru.timeconqueror.timecore.animation;

import lombok.*;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;
import ru.timeconqueror.timecore.animation.component.LoopMode;
import ru.timeconqueror.timecore.animation.watcher.Timeline;
import ru.timeconqueror.timecore.api.animation.Animation;
import ru.timeconqueror.timecore.api.animation.AnimationConstants;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@EqualsAndHashCode
public class AnimationData {
    @Getter
    final Animation animation;
    @Getter
    boolean ignorable = true;
    Integer startAnimationTime = null;
    @Getter
    int transitionTime = AnimationConstants.BASIC_TRANSITION_TIME;
    @Getter
    float speed = 1F;
    @Getter
    boolean noTransitionToNone;
    @Getter
    boolean reversed;
    @Nullable
    LoopMode loopMode = null;

    public static void encode(AnimationData animationData, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(animationData.getAnimation().getId());
        buffer.writeFloat(animationData.getSpeed());
        buffer.writeInt(animationData.getTransitionTime());

        buffer.writeBoolean(animationData.startAnimationTime != null);
        if (animationData.startAnimationTime != null) {
            buffer.writeVarInt(animationData.startAnimationTime);
        }
        buffer.writeBoolean(animationData.isIgnorable());
        buffer.writeBoolean(animationData.noTransitionToNone);
        buffer.writeBoolean(animationData.reversed);

        buffer.writeBoolean(animationData.loopMode != null);
        if (animationData.loopMode != null) {
            buffer.writeVarInt(LoopMode.ORDINAL_LOOKUP.from(animationData.loopMode));
        }
    }

    public static AnimationData decode(FriendlyByteBuf buffer) {
        Animation animation = AnimationRegistry.getAnimation(buffer.readResourceLocation());

        AnimationData animationData = new AnimationData(animation);

        animationData.speed = buffer.readFloat();
        animationData.transitionTime = buffer.readInt();
        if (buffer.readBoolean()) {
            animationData.startAnimationTime = buffer.readVarInt();
        }
        animationData.ignorable = buffer.readBoolean();
        animationData.noTransitionToNone = buffer.readBoolean();
        animationData.reversed = buffer.readBoolean();

        boolean hasLoopMode = buffer.readBoolean();
        if (hasLoopMode) {
            animationData.loopMode = LoopMode.ORDINAL_LOOKUP.by(buffer.readVarInt());
        }

        return animationData;
    }


    public int getAnimationLength() {
        return animation.getLength();
    }

    public int getStartAnimationTime() {
        return startAnimationTime != null ? startAnimationTime : isReversed() ? this.getAnimationLength() : 0;
    }

    public LoopMode getLoopMode() {
        return loopMode != null ? loopMode : animation.getLoopMode();
    }

    public AnimationData copy() {
        AnimationData animationData = new AnimationData(animation);
        animationData.speed = this.speed;
        animationData.ignorable = this.ignorable;
        animationData.startAnimationTime = startAnimationTime;
        animationData.transitionTime = this.transitionTime;
        animationData.noTransitionToNone = noTransitionToNone;
        animationData.reversed = reversed;
        animationData.loopMode = loopMode;

        return animationData;
    }

    public int getElapsedLengthTillFirstBoundary() {
        return Timeline.getFirstBoundaryElapsedLength(getAnimationLength(), getStartAnimationTime(), getSpeed(), isReversed());
    }
}