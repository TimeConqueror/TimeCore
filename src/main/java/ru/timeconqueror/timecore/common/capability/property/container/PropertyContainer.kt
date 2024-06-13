package ru.timeconqueror.timecore.common.capability.property.container

import net.minecraft.nbt.CompoundTag
import ru.timeconqueror.timecore.api.common.blockentity.SerializationType
import ru.timeconqueror.timecore.common.capability.property.CoffeeObservableList
import ru.timeconqueror.timecore.common.capability.property.CoffeeProperty
import ru.timeconqueror.timecore.common.capability.property.serializer.*
import java.util.function.Predicate
import java.util.function.Supplier

open class PropertyContainer {
    private val properties = mutableListOf<CoffeeProperty<*>>()
    private val containers = hashMapOf<String, PropertyContainer>()

    protected open fun <V> prop(name: String, value: V, serializer: IPropertySerializer<V>): CoffeeProperty<V> {
        val prop = CoffeeProperty(name, value, serializer)
        properties.add(prop)
        return prop
    }

    protected fun prop(name: String, value: Int): CoffeeProperty<Int> {
        return prop(name, value, IntPropertySerializer)
    }

    protected fun prop(name: String, value: Long): CoffeeProperty<Long> {
        return prop(name, value, LongPropertySerializer)
    }

    protected fun prop(name: String, value: Float): CoffeeProperty<Float> {
        return prop(name, value, FloatPropertySerializer)
    }

    protected fun prop(name: String, value: Double): CoffeeProperty<Double> {
        return prop(name, value, DoublePropertySerializer)
    }

    protected fun prop(name: String, value: Boolean): CoffeeProperty<Boolean> {
        return prop(name, value, BooleanPropertySerializer)
    }

    protected fun prop(name: String, value: String): CoffeeProperty<String> {
        return prop(name, value, StringPropertySerializer)
    }

    protected fun <T> prop(name: String, listCreator: Supplier<List<T>>,
                           entrySerializer: IPropertySerializer<T>): CoffeeProperty<CoffeeObservableList<T>> {
        return prop(name, CoffeeObservableList.observe(listCreator.get()), ObservableListSerializer(listCreator, entrySerializer));
    }

    protected fun nullableProp(name: String, value: Int?): CoffeeProperty<Int?> {
        return prop(name, value, IntPropertySerializer.Nullable)
    }

    protected fun nullableProp(name: String, value: Long?): CoffeeProperty<Long?> {
        return prop(name, value, LongPropertySerializer.Nullable)
    }

    protected fun nullableProp(name: String, value: Float?): CoffeeProperty<Float?> {
        return prop(name, value, FloatPropertySerializer.Nullable)
    }

    protected fun nullableProp(name: String, value: Double?): CoffeeProperty<Double?> {
        return prop(name, value, DoublePropertySerializer.Nullable)
    }

    protected fun nullableProp(name: String, value: Boolean?): CoffeeProperty<Boolean?> {
        return prop(name, value, BooleanPropertySerializer.Nullable)
    }

    protected fun nullableProp(name: String, value: String?): CoffeeProperty<String?> {
        return prop(name, value, StringPropertySerializer.Nullable)
    }

    protected fun <T : PropertyContainer> container(name: String, value: T): T {
        if (containers.put(name, value) != null) {
            throw IllegalArgumentException("The container with name '$name' has been already registered!")
        }
        return value
    }

    fun serialize(
        serializePredicate: Predicate<CoffeeProperty<*>>,
        nbt: CompoundTag,
        clientSide: Boolean,
        type: SerializationType
    ): Boolean {
        var hasChanges = false
        for (property in properties) {
            if (property.clientDependent == clientSide && serializePredicate.test(property)) {
                property.serialize(nbt, type)
                hasChanges = true
            }
        }
        for ((name, container) in containers) {
            val containerNBT = CompoundTag()
            if (container.serialize(serializePredicate, containerNBT, clientSide, type)) {
                nbt.put(name, containerNBT)
                hasChanges = true
            }
        }
        return hasChanges
    }

    fun deserialize(nbt: CompoundTag) {
        for (property in properties) {
            property.deserialize(nbt)
        }
        for ((name, container) in containers) {
            if (nbt.contains(name)) {
                container.deserialize(nbt.getCompound(name))
            }
        }
    }

    fun deserialize(nbt: CompoundTag, sentFromClient: Boolean) {
        for (property in properties) {
            property.deserialize(nbt, sentFromClient)
        }
        for ((name, container) in containers) {
            if (nbt.contains(name)) {
                container.deserialize(nbt.getCompound(name), sentFromClient)
            }
        }
    }
}