package ru.timeconqueror.timecore.internal.common.packet.animation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import ru.timeconqueror.timecore.animation.network.codec.LevelObjectCodec;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.animation.AnimationManager;

public class S2CStopAnimationMsg extends S2CAnimationMsg {
    private final int transitionTime;
    private final String layerName;

    public S2CStopAnimationMsg(LevelObjectCodec<?> ownerCodec, String layerName, int transitionTime) {
        super(ownerCodec);
        this.layerName = layerName;
        this.transitionTime = transitionTime;
    }

    public static class Handler extends S2CAnimationMsg.Handler<S2CStopAnimationMsg> {

        @Override
        public void onPacket(S2CStopAnimationMsg packet, AnimatedObject<?> owner, NetworkEvent.Context ctx) {
            AnimationManager animationManager = owner.animationSystem().getAnimationManager();
            animationManager.stopAnimation(packet.layerName, packet.transitionTime);
        }

        @Override
        public void encodeExtra(S2CStopAnimationMsg packet, FriendlyByteBuf buffer) {
            buffer.writeInt(packet.transitionTime);
            buffer.writeUtf(packet.layerName);
        }

        @Override
        public S2CStopAnimationMsg decodeWithExtraData(LevelObjectCodec<?> codecSupplier, FriendlyByteBuf buffer) {
            int transitionTime = buffer.readInt();
            String layerName = buffer.readUtf();
            return new S2CStopAnimationMsg(codecSupplier, layerName, transitionTime);
        }
    }
}
