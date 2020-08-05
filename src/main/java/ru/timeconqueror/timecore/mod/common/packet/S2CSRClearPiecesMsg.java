package ru.timeconqueror.timecore.mod.common.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import ru.timeconqueror.timecore.TimeCore;
import ru.timeconqueror.timecore.api.common.packet.ITimePacket;
import ru.timeconqueror.timecore.devtools.StructureRevealer;

import java.util.Optional;
import java.util.function.Supplier;

public class S2CSRClearPiecesMsg implements ITimePacket {
    @Override
    public @NotNull LogicalSide getReceptionSide() {
        return LogicalSide.CLIENT;
    }

    public static class Handler implements ITimePacketHandler<S2CSRClearPiecesMsg> {
        @Override
        public void encode(S2CSRClearPiecesMsg packet, PacketBuffer buffer) {

        }

        @NotNull
        @Override
        public S2CSRClearPiecesMsg decode(PacketBuffer buffer) {
            return new S2CSRClearPiecesMsg();
        }

        @Override
        public void onPacketReceived(S2CSRClearPiecesMsg packet, Supplier<NetworkEvent.Context> contextSupplier) {
            Optional<StructureRevealer> instance = StructureRevealer.getInstance();
            if (instance.isPresent()) {
                instance.get().structureRenderer.getTrackedStructurePieces().clear();
            } else {
                TimeCore.LOGGER.warn("Server has sent you a structure revealing packet, but structure revealer is turned off on client!");
            }
        }
    }
}