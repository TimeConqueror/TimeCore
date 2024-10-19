package ru.timeconqueror.timecore.internal.common.packet.animation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import ru.timeconqueror.timecore.animation.BaseAnimationManager;
import ru.timeconqueror.timecore.animation.network.AnimationState;
import ru.timeconqueror.timecore.animation.network.codec.LevelObjectCodec;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.util.holder.Pair;

import java.util.ArrayList;
import java.util.List;

public class S2CSyncAnimationsMsg extends S2CAnimationMsg {
    private final List<Pair<String, AnimationState>> statesByLayer;

    public S2CSyncAnimationsMsg(LevelObjectCodec<?> codecSupplier, List<Pair<String, AnimationState>> statesByLayer) {
        super(codecSupplier);
        this.statesByLayer = statesByLayer;
    }

    public static class Handler extends S2CAnimationMsg.Handler<S2CSyncAnimationsMsg> {
        @Override
        public void encodeExtra(S2CSyncAnimationsMsg packet, FriendlyByteBuf buffer) {
            buffer.writeVarInt(packet.statesByLayer.size());
            for (Pair<String, AnimationState> e : packet.statesByLayer) {
                buffer.writeUtf(e.left());
                e.right().serialize(buffer);
            }
        }

        @Override
        public S2CSyncAnimationsMsg decodeWithExtraData(LevelObjectCodec<?> codecSupplier, FriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            var statesByLayer = new ArrayList<Pair<String, AnimationState>>(size);
            for (int i = 0; i < size; i++) {
                statesByLayer.add(Pair.of(buffer.readUtf(), AnimationState.deserialize(buffer)));
            }
            return new S2CSyncAnimationsMsg(codecSupplier, statesByLayer);
        }

        @Override
        public void onPacket(S2CSyncAnimationsMsg packet, AnimatedObject<?> owner, NetworkEvent.Context ctx) {
            ((BaseAnimationManager) owner.animationSystem().getAnimationManager()).setLayersState(packet.statesByLayer);
        }
    }
}
