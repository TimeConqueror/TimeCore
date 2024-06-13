package ru.timeconqueror.timecore.common.capability.property

import net.minecraft.nbt.CompoundTag
import ru.timeconqueror.timecore.api.common.blockentity.SerializationType
import ru.timeconqueror.timecore.common.capability.property.serializer.IPropertySerializer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class CoffeeProperty<T>(val name: String, private var value: T, val serializer: IPropertySerializer<T>) :
    ReadWriteProperty<Any, T> {

    private var externalChangable = value is IChangable

    var clientDependent = false
        private set

    private var shouldBeSynced = false

    var changed = false
        get() = externalChangable && (value as IChangable).changed || field
        set(value) {
            if (externalChangable) {
                (this.value as IChangable).changed = value
            }
            field = value
        }

    override operator fun getValue(thisRef: Any, property: KProperty<*>) = get()

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) = set(value)

    fun get() = value
    fun set(value: T) {
        this.changed = true
        this.value = value
    }

    fun serialize(nbt: CompoundTag, type: SerializationType) {
        if (type == SerializationType.SAVE || shouldBeSynced) {
            serializer.serialize(name, value, nbt)
        }
    }

    fun deserialize(nbt: CompoundTag) {
        if (nbt.contains(name)) {
            this.value = serializer.deserialize(name, nbt)
            this.changed = false
        }
    }

    fun deserialize(nbt: CompoundTag, fromClient: Boolean) {
        if (fromClient == clientDependent) {
            deserialize(nbt)
        }
    }

    fun clientDependent(): CoffeeProperty<T> {
        clientDependent = true
        return this
    }

    fun synced(): CoffeeProperty<T> {
        shouldBeSynced = true
        return this
    }
}