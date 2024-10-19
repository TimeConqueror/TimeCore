package ru.timeconqueror.timecore.animation.network;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraftforge.network.PacketDistributor;
import ru.timeconqueror.timecore.animation.network.codec.LevelObjectCodec;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.animation.AnimationScript;
import ru.timeconqueror.timecore.api.util.holder.Pair;
import ru.timeconqueror.timecore.internal.common.packet.InternalPacketManager;
import ru.timeconqueror.timecore.internal.common.packet.animation.S2CStartAnimationMsg;
import ru.timeconqueror.timecore.internal.common.packet.animation.S2CStopAnimationMsg;
import ru.timeconqueror.timecore.internal.common.packet.animation.S2CSyncAnimationsMsg;

import java.util.List;

@AllArgsConstructor
public class NetworkDispatcherInstance<T extends AnimatedObject<T>> {
    private final NetworkDispatcher<T> networkDispatcher;
    @Getter
    private final T animatedObject;

    public void sendSetAnimationPacket(AnimationScript animationScript, String layerName) {
        InternalPacketManager.INSTANCE.send(getPacketTarget(), new S2CStartAnimationMsg(getCodecSupplier(), layerName, animationScript));
    }

    public void sendStopAnimationPacket(String layerName, int transitionTime) {
        InternalPacketManager.INSTANCE.send(getPacketTarget(), new S2CStopAnimationMsg(getCodecSupplier(), layerName, transitionTime));
    }

    public void sendSyncAnimationsPacket(List<Pair<String, AnimationState>> statesByLayer) {
        InternalPacketManager.INSTANCE.send(getPacketTarget(), new S2CSyncAnimationsMsg(getCodecSupplier(), statesByLayer));
    }

    protected LevelObjectCodec<?> getCodecSupplier() {
        return networkDispatcher.getCodec(animatedObject);
    }

    protected PacketDistributor.PacketTarget getPacketTarget() {
        return networkDispatcher.getPacketTarget(animatedObject);
    }
}
