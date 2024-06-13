package ru.timeconqueror.timecore.api.registry;

import lombok.extern.log4j.Log4j2;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import org.jetbrains.annotations.Nullable;
import ru.timeconqueror.timecore.api.util.holder.Pair;
import ru.timeconqueror.timecore.storage.Features;
import ru.timeconqueror.timecore.storage.Storage;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

//TODO auto unreg
@Log4j2
public abstract class TimeRegister {
    private final String modId;
    protected final Features modFeatures;
    @Nullable
    private Class<?> owner;
    private final AtomicReference<RuntimeException> error = new AtomicReference<>();

    public TimeRegister(String modId) {
        this.modId = modId;
        modFeatures = Storage.getFeatures(modId);
    }

    public void regToBus(IEventBus modEventBus) {
        modEventBus.addListener(this::handleLoadException);
    }

    private void handleLoadException(FMLLoadCompleteEvent event) {
        RuntimeException e = error.get();

        if (e != null) {
            throw e;
        }
    }

    public String getModId() {
        return modId;
    }

    public void setOwner(Class<?> owner) {
        this.owner = owner;
    }

    public @Nullable Class<?> getOwner() {
        return owner;
    }

    /**
     * Enqueues and catches the possible exceptions.
     */
    protected void enqueueWork(ParallelDispatchEvent event, Runnable action) {
        event.enqueueWork(catchable(event, action));
    }

    protected Runnable catchable(Event event, Runnable runnable) {
        return () -> catchErrors(event, runnable);
    }

    public void catchErrors(Event event, Runnable runnable) {
        catchErrors(event.getClass().getName(), runnable);
    }

    public void catchErrors(String action, Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            String culpritInfo = getOwner() != null ? "Currently handling stuff from class: " + getOwner().getName() : "Unknown owner";
            storeAndLogException(new RuntimeException("Caught exception during " + action + ". " + culpritInfo, e));
        }
    }

    public void catchErrors(String action, Runnable runnable, Supplier<List<Pair<?, ?>>> extraInfo) {
        try {
            runnable.run();
        } catch (Throwable e) {
            String culpritInfo = getOwner() != null ? "Currently handling stuff from class: " + getOwner().getName() + "." : "Unknown owner.";
            String extra = "Extra Info:\n" + extraInfo.get().stream().map(pair ->pair.left() + ": " + pair.right() + "\n").collect(Collectors.joining());
            RuntimeException exception = new RuntimeException("Caught exception during " + action + ". \n" + culpritInfo + "\n" + extra, e);
            storeAndLogException(exception);
        }
    }

    protected void storeAndLogException(RuntimeException exception) {
        log.error("Exception in TimeRegister", exception);
        error.compareAndSet(null, exception);
    }
}
