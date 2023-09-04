package ru.timeconqueror.timecore.animation.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import ru.timeconqueror.timecore.animation.watcher.AnimationTicker;
import ru.timeconqueror.timecore.animation.watcher.AnimationTickerImpl;
import ru.timeconqueror.timecore.animation.watcher.EmptyAnimationTicker;
import ru.timeconqueror.timecore.animation.watcher.TransitionTicker;
import ru.timeconqueror.timecore.api.util.holder.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TickerSerializers {

    public static void serializeTickers(List<Pair<String, AnimationTicker>> tickersByLayer, FriendlyByteBuf buffer) {
        buffer.writeInt(tickersByLayer.size());

        for (Pair<String, AnimationTicker> pair : tickersByLayer) {
            buffer.writeUtf(pair.left());
            serializeTicker(pair.right(), buffer);
        }
    }

    public static List<Pair<String, AnimationTicker>> deserializeTickers(FriendlyByteBuf buffer) {
        int layerCount = buffer.readInt();

        List<Pair<String, AnimationTicker>> tickersByLayer = new ArrayList<>(layerCount);

        for (int i = 0; i < layerCount; i++) {
            String layerName = buffer.readUtf();
            AnimationTicker ticker = deserializeTicker(buffer);
            tickersByLayer.add(Pair.of(layerName, ticker));
        }

        return tickersByLayer;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void serializeTicker(@NotNull AnimationTicker ticker, FriendlyByteBuf buffer) {
        TickerType type = getType(ticker);
        if (type == TickerType.UNKNOWN) throw new IllegalArgumentException("No ticker type for: " + ticker.getClass());

        buffer.writeVarInt(type.ordinal());
        AnimationTickerSerializer serializer = type.getSerializer();
        serializer.serialize(ticker, buffer);
    }

    public static AnimationTicker deserializeTicker(FriendlyByteBuf buffer) {
        TickerType type = TickerType.VALID[buffer.readVarInt()];
        return type.serializer.deserialize(buffer);
    }

    private static TickerType getType(AnimationTicker ticker) {
        for (TickerType type : TickerType.VALID) {
            if (type.getTickerClass().isInstance(ticker)) {
                return type;
            }
        }

        return TickerType.UNKNOWN;
    }

    @AllArgsConstructor
    public enum TickerType {
        EMPTY(EmptyAnimationTicker.class, new EmptyAnimationTicker.Serializer()),
        ANIMATION(AnimationTickerImpl.class, new AnimationTickerImpl.Serializer()),
        TRANSITION(TransitionTicker.class, new TransitionTicker.Serializer()),
        // should be last
        UNKNOWN(null, null);

        public static final TickerType[] VALID = Arrays.stream(TickerType.values())
                .filter(tickerType -> tickerType != UNKNOWN)
                .toArray(TickerType[]::new);
        @Getter
        private final Class<?> tickerClass;
        @Getter
        private final AnimationTickerSerializer<?> serializer;
    }
}