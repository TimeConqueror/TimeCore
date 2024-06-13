package ru.timeconqueror.timecore.common.capability

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.Level
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.network.simple.SimpleChannel
import ru.timeconqueror.timecore.api.common.blockentity.SerializationType
import ru.timeconqueror.timecore.common.capability.owner.serializer.CapabilityOwnerCodec
import ru.timeconqueror.timecore.common.capability.property.CoffeeProperty
import ru.timeconqueror.timecore.common.capability.property.container.PropertyContainer
import ru.timeconqueror.timecore.internal.common.packet.CoffeeCapabilityDataPacket
import ru.timeconqueror.timecore.internal.common.packet.InternalPacketManager
import java.util.function.Predicate
import javax.annotation.ParametersAreNonnullByDefault

//TODO property enums,
//TODO custom properties,
//TODO move all extra attach (keep after death, etc) to properties
//TODO doNotSync, doNotSave
//ToDO check client dependent caps being saved on server?
//TODO store owner inside the cap so people don't bother creating their own sendAllData & sendChangesToClients without args
@ParametersAreNonnullByDefault
abstract class CoffeeCapabilityInstance<T : ICapabilityProvider> : PropertyContainer(), INBTSerializable<CompoundTag> {

    abstract fun getOwnerSerializer(): CapabilityOwnerCodec<T>
    abstract fun getCapability(): Capability<out CoffeeCapabilityInstance<T>>

    /**
     * Checks if properties of capability has changed and if yes, sends them.
     * Works in both directions.
     */
    fun detectAndSendChanges(level: Level, owner: T) {
        sendData(level, owner) { prop ->
            if (prop.changed) {
                prop.changed = false
                true
            } else false
        }
    }

    /**
     * Synchronizes all capability data.
     * Works in both directions.
     */
    fun sendAllData(level: Level, owner: T) {
        sendData(level, owner) { true }
    }

    abstract fun sendChangesToClient(channel: SimpleChannel, data: Any);

    private fun sendData(level: Level, owner: T, predicate: Predicate<CoffeeProperty<*>>) {
        val clientSide = level.isClientSide()

        val message = createDataPacket(level, owner, clientSide, predicate)

        if (message != null) {
            if (clientSide) {
                InternalPacketManager.INSTANCE.sendToServer(message)
            } else {
                sendChangesToClient(InternalPacketManager.INSTANCE, message)
            }
        }
    }

    private fun createDataPacket(
        level: Level,
        owner: T,
        clientSide: Boolean,
        syncPredicate: Predicate<CoffeeProperty<*>>
    ) = CoffeeCapabilityDataPacket.create(level, owner, this, clientSide, syncPredicate)

    override fun serializeNBT(): CompoundTag {
        val compound = CompoundTag()
        serialize({ true }, compound, false, SerializationType.SAVE)
        return compound
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        deserialize(nbt)
    }
}