package ru.timeconqueror.timecore.registry.newreg;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import ru.timeconqueror.timecore.util.Pair;
import ru.timeconqueror.timecore.util.Temporal;

import java.util.ArrayList;
import java.util.List;

/**
 * You can {@link SimpleVanillaRegister} it as a wrapper for all vanilla registries, which don't have forge wrapper.
 * All values will be registered on the main thread on {@link FMLCommonSetupEvent}
 */
public abstract class VanillaRegister<T> extends TimeRegister {
    private final Registry<? super T> registry;
    private final Temporal<List<Pair<ResourceLocation, T>>> entries = Temporal.of(new ArrayList<>());

    public VanillaRegister(String modId, Registry<? super T> registry) {
        super(modId);
        this.registry = registry;
    }

    protected void addEntry(String name, T value) {
        entries.get().add(Pair.of(new ResourceLocation(getModId(), name), value));
    }

    @Override
    public void regToBus(IEventBus bus) {
        bus.addListener(this::onSetup);
    }

    private void onSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> entries.transferAndRemove(entries -> entries.forEach(entry -> Registry.register(registry, entry.getA(), entry.getB()))));
    }
}