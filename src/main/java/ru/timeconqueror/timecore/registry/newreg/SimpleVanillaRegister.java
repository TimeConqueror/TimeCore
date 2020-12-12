package ru.timeconqueror.timecore.registry.newreg;

import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * You can use it as a wrapper for all vanilla registries, which don't have forge wrapper.
 * All values will be registered on the main thread on {@link FMLCommonSetupEvent}
 */
public class SimpleVanillaRegister<T> extends VanillaRegister<T> {
    public SimpleVanillaRegister(String modId, Registry<T> registry) {
        super(modId, registry);
    }

    /**
     * Adds value to the delayed registry array, all entries from which will be registered later.
     *
     * @param name  The value's name, will automatically have the modid as a namespace.
     * @param value value to be registered.
     */
    public T register(String name, T value) {
        addEntry(name, value);

        return value;
    }
}
