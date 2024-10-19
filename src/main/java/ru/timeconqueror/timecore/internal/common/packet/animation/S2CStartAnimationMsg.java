package ru.timeconqueror.timecore.internal.common.packet.animation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import ru.timeconqueror.timecore.TimeCore;
import ru.timeconqueror.timecore.animation.AnimationScriptImpl;
import ru.timeconqueror.timecore.animation.network.codec.LevelObjectCodec;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.animation.Animation;
import ru.timeconqueror.timecore.api.animation.AnimationScript;

public class S2CStartAnimationMsg extends S2CAnimationMsg {
    private final AnimationScript animationScript;
    private final String layerName;

    public S2CStartAnimationMsg(LevelObjectCodec<?> ownerCodec, String layerName, AnimationScript animationScript) {
        super(ownerCodec);
        this.layerName = layerName;
        this.animationScript = animationScript;
    }

    public static class Handler extends S2CAnimationMsg.Handler<S2CStartAnimationMsg> {
        @Override
        public void encodeExtra(S2CStartAnimationMsg packet, FriendlyByteBuf buffer) {
            AnimationScriptImpl.encode(packet.animationScript, buffer);
            buffer.writeUtf(packet.layerName);
        }

        @Override
        public S2CStartAnimationMsg decodeWithExtraData(LevelObjectCodec<?> codecSupplier, FriendlyByteBuf buffer) {
            AnimationScript animationScript = AnimationScriptImpl.decode(buffer);
            String layerName = buffer.readUtf();
            return new S2CStartAnimationMsg(codecSupplier, layerName, animationScript);
        }

        @Override
        public void onPacket(S2CStartAnimationMsg packet, AnimatedObject<?> owner, NetworkEvent.Context ctx) {
            AnimationScript animationScript = packet.animationScript;

            Animation animation = animationScript.getAnimationData().getAnimation();

            if (animation == null) {
                TimeCore.LOGGER.error("Client received an animation, which is not registered on client.");
                return;
            }

            owner.animationSystem().getAnimationManager().startAnimationScript(animationScript, packet.layerName);
        }
    }
}
