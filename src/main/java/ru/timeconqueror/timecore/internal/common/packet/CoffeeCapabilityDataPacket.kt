package ru.timeconqueror.timecore.internal.common.packet

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.level.Level
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.network.NetworkEvent
import ru.timeconqueror.timecore.TimeCore
import ru.timeconqueror.timecore.api.common.blockentity.SerializationType
import ru.timeconqueror.timecore.api.common.packet.ITimePacketHandler
import ru.timeconqueror.timecore.common.capability.CoffeeCapabilityInstance
import ru.timeconqueror.timecore.common.capability.property.CoffeeProperty
import java.util.function.Predicate

sealed class CoffeeCapabilityDataPacket(
    val capabilityName: String,
    val ownerData: CompoundTag,
    val capabilityData: CompoundTag
) {

    companion object {
        fun <T : ICapabilityProvider> create(
            world: Level,
            owner: T,
            cap: CoffeeCapabilityInstance<T>,
            capabilityData: CompoundTag,
            clientSide: Boolean
        ): CoffeeCapabilityDataPacket {
            return create(clientSide, cap.getCapability().name, CompoundTag().apply {
                cap.getOwnerSerializer().serialize(world, owner, this)
            }, capabilityData)
        }

        private fun create(
            clientSide: Boolean,
            capabilityName: String,
            ownerData: CompoundTag,
            capabilityData: CompoundTag
        ): CoffeeCapabilityDataPacket {
            return if (clientSide) {
                C2SCoffeeCapabilityDataPacket(capabilityName, ownerData, capabilityData)
            } else {
                S2CCoffeeCapabilityDataPacket(capabilityName, ownerData, capabilityData)
            }
        }

        fun <T : ICapabilityProvider> create(
            world: Level,
            owner: T,
            cap: CoffeeCapabilityInstance<T>,
            clientSide: Boolean,
            syncPredicate: Predicate<CoffeeProperty<*>>
        ): CoffeeCapabilityDataPacket? {
            val nbt = CompoundTag()
            return if (cap.serialize(syncPredicate, nbt, clientSide, SerializationType.SYNC)) {
                create(world, owner, cap, nbt, clientSide)
            } else null
        }

        private fun handlePacket(packet: CoffeeCapabilityDataPacket, world: Level, sentFromClient: Boolean) {
            val capability = TimeCore.INSTANCE.capabilityManager.getAttachableCoffeeCapability(packet.capabilityName)
            if (capability != null) {
                val ownerCodec = capability.owner().serializer
                val owner: ICapabilityProvider? = ownerCodec.deserialize(world, packet.ownerData)

                if (owner != null) {
                    val cap: LazyOptional<out CoffeeCapabilityInstance<*>> =
                        owner.getCapability(capability.capability(), null)

                    cap.ifPresent {
                        it.deserialize(packet.capabilityData, sentFromClient)
                    }
                }
            }
        }
    }

    sealed class Handler<T : CoffeeCapabilityDataPacket> :
        ITimePacketHandler<T> {
        final override fun encode(dataMsg: T, buf: FriendlyByteBuf) {
            buf.writeBoolean(dataMsg is C2SCoffeeCapabilityDataPacket)
            buf.writeUtf(dataMsg.capabilityName)
            buf.writeNbt(dataMsg.ownerData)
            buf.writeNbt(dataMsg.capabilityData)
        }

        final override fun decode(buf: FriendlyByteBuf): T {
            val sentFromClient = buf.readBoolean()
            return create(sentFromClient, buf.readUtf(), buf.readNbt()!!, buf.readNbt()!!) as T
        }

        final override fun handle(msg: T, ctx: NetworkEvent.Context) {
            val sentFromClient = ctx.direction.receptionSide == LogicalSide.SERVER
            handlePacket(msg, getWorld(ctx), sentFromClient)
        }

        object ServerHandler : CoffeeCapabilityDataPacket.Handler<C2SCoffeeCapabilityDataPacket>()
        object ClientHandler : CoffeeCapabilityDataPacket.Handler<S2CCoffeeCapabilityDataPacket>()
    }
}

class C2SCoffeeCapabilityDataPacket(capabilityName: String, ownerData: CompoundTag, capabilityData: CompoundTag) :
    CoffeeCapabilityDataPacket(capabilityName, ownerData, capabilityData)

class S2CCoffeeCapabilityDataPacket(capabilityName: String, ownerData: CompoundTag, capabilityData: CompoundTag) :
    CoffeeCapabilityDataPacket(capabilityName, ownerData, capabilityData)