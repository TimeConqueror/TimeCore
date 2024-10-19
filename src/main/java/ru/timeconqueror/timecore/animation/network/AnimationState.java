package ru.timeconqueror.timecore.animation.network;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import ru.timeconqueror.timecore.animation.AnimationScriptImpl;
import ru.timeconqueror.timecore.api.animation.AnimationScript;

public abstract sealed class AnimationState {

    public static AnimationState deserialize(FriendlyByteBuf buf) {
        switch (buf.readByte()) {
            case TransitionState.KEY -> {
                return new TransitionState(buf);
            }
            case ActiveState.KEY -> {
                return new ActiveState(buf);
            }
            default -> {
                return EmptyState.INSTANCE;
            }
        }
    }

    public abstract void serialize(FriendlyByteBuf buf);

    @AllArgsConstructor
    @Getter
    public static final class TransitionState extends AnimationState {
        private static final int KEY = 2;
        private final AnimationState source;
        private final AnimationState destination;
        private final int transitionLength;
        private final int elapsedTime;
        private final int sourceTimeInFreeze;

        private TransitionState(FriendlyByteBuf buf) {
            source = deserialize(buf);
            destination = deserialize(buf);

            transitionLength = buf.readInt();
            elapsedTime = buf.readInt();
            sourceTimeInFreeze = buf.readInt();
        }

        @Override
        public void serialize(FriendlyByteBuf buf) {
            buf.writeByte(KEY);

            source.serialize(buf);
            destination.serialize(buf);
            buf.writeInt(transitionLength);
            buf.writeInt(elapsedTime);
            buf.writeInt(sourceTimeInFreeze);
        }
    }

    @AllArgsConstructor
    @Getter
    public static final class ActiveState extends AnimationState {
        private static final int KEY = 1;
        private final AnimationScript animationScript;
        private final int elapsedTime;

        private ActiveState(FriendlyByteBuf buf) {
            this.animationScript = AnimationScriptImpl.decode(buf);
            this.elapsedTime = buf.readVarInt();
        }

        @Override
        public void serialize(FriendlyByteBuf buf) {
            buf.writeByte(KEY);
            AnimationScriptImpl.encode(animationScript, buf);
            buf.writeVarInt(elapsedTime);
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class EmptyState extends AnimationState {
        public static final EmptyState INSTANCE = new EmptyState();
        private static final int KEY = 0;

        @Override
        public void serialize(FriendlyByteBuf buf) {
            buf.writeByte(KEY);
        }
    }
}
