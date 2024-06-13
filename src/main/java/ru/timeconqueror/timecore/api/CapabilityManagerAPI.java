package ru.timeconqueror.timecore.api;

import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import ru.timeconqueror.timecore.TimeCore;
import ru.timeconqueror.timecore.common.capability.CapabilityManager;
import ru.timeconqueror.timecore.common.capability.CoffeeCapabilityInstance;
import ru.timeconqueror.timecore.common.capability.listener.EntityCapSyncOnStartTrackListener;
import ru.timeconqueror.timecore.common.capability.owner.CapabilityOwner;
import ru.timeconqueror.timecore.common.capability.owner.attach.getter.CoffeeCapabilityGetter;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CapabilityManagerAPI {
    /**
     * Register the attacher, which will bind coffee capability instance to owner upon its creation. <br>
     * <i>Dynamic</i> means that the owner can return different capability instances depending on provided {@link Direction}
     * as it works for ItemHandler in block entities for example.
     * <br>
     * Should be called during {@link FMLCommonSetupEvent} inside {@link FMLCommonSetupEvent#enqueueWork}
     *
     * @param owner       object you want capability to be attached to
     * @param capability  registered capability
     * @param ownerFilter allows to filter the objects, to which you can attach capability. <br>
     *                    E.g. Attach capability to all entities who are players: <pre>{@code () -> entity instanceof Player}</pre>
     * @param getters     determine which capability object should return depending on its type and {@link Direction}.
     * @see CapabilityManager#addDefaultAttachers()
     */
    public static <T extends ICapabilityProvider, C extends CoffeeCapabilityInstance<T>> void registerDynamicCoffeeAttacher(CapabilityOwner<T> owner, Capability<C> capability, Predicate<T> ownerFilter, Supplier<CoffeeCapabilityGetter<T, C>> getters) {
        TimeCore.INSTANCE.getCapabilityManager().registerDynamicCoffeeAttacher(owner, capability, ownerFilter, getters);
    }

    /**
     * Register the attacher, which will bind vanilla capability instance to owner upon its creation. <br>
     * <i>Dynamic</i> means that the owner can return different capability instances depending on provided {@link Direction}
     * as it works for ItemHandler in block entities for example.
     * <br>
     * Should be called during {@link FMLCommonSetupEvent} inside {@link FMLCommonSetupEvent#enqueueWork}
     *
     * @param owner       object you want capability to be attached to
     * @param capability  registered capability
     * @param ownerFilter allows to filter the objects, to which you can attach capability. <br>
     *                    E.g. Attach capability to all entities who are players: <pre>{@code () -> entity instanceof Player}</pre>
     * @param getters     determine which capability object should return depending on its type and {@link Direction}.
     * @see CapabilityManager#addDefaultAttachers()
     */
    public static <T extends ICapabilityProvider, C> void registerDynamicAttacher(CapabilityOwner<T> owner, Capability<C> capability, Predicate<T> ownerFilter, Supplier<CoffeeCapabilityGetter<T, C>> getters) {
        TimeCore.INSTANCE.getCapabilityManager().registerDynamicCapabilityAttacher(owner, capability, ownerFilter, getters);
    }

    /**
     * Register the attacher, which will bind coffee capability instance to owner upon its creation. <br>
     * <i>Static</i> means that the owner will return the same capability instance regardless of provided {@link Direction}
     * (which is true for almost all available owners ({@link ICapabilityProvider}s) except block entities).
     * <br>
     * Should be called during {@link FMLCommonSetupEvent} inside {@link FMLCommonSetupEvent#enqueueWork}
     *
     * @param owner       object you want capability to be attached to
     * @param capability  registered capability
     * @param ownerFilter allows to filter the objects, to which you can attach capability. <br>
     *                    E.g. Attach capability to all entities who are players: <pre>{@code () -> entity instanceof Player}</pre>
     * @param capFactory  method, which controls how the capability should be created upon owner creation.
     * @see CapabilityManager#addDefaultAttachers()
     */
    public static <T extends ICapabilityProvider, C extends CoffeeCapabilityInstance<T>> void registerStaticCoffeeAttacher(CapabilityOwner<T> owner, Capability<C> capability, Predicate<T> ownerFilter, Function<T, C> capFactory) {
        TimeCore.INSTANCE.getCapabilityManager().registerStaticCoffeeCapabilityAttacher(owner, capability, ownerFilter, capFactory);
    }

    /**
     * Register the attacher, which will bind vanilla capability instance to owner upon its creation. <br>
     * <i>Static</i> means that the owner will return the same capability instance regardless of provided {@link Direction}
     * (which is true for almost all available owners ({@link ICapabilityProvider}s) except block entities).
     * <br>
     * Should be called during {@link FMLCommonSetupEvent} inside {@link FMLCommonSetupEvent#enqueueWork}
     *
     * @param owner       object you want capability to be attached to
     * @param capability  registered capability
     * @param ownerFilter allows to filter the objects, to which you can attach capability. <br>
     *                    E.g. Attach capability to all entities who are players: <pre>{@code () -> entity instanceof Player}</pre>
     * @param capFactory  method, which controls how the capability should be created upon owner creation.
     * @see CapabilityManager#addDefaultAttachers()
     */
    public static <T extends ICapabilityProvider, C> void registerStaticAttacher(CapabilityOwner<T> owner, Capability<C> capability, Predicate<T> ownerFilter, Function<T, C> capFactory) {
        TimeCore.INSTANCE.getCapabilityManager().registerStaticCapabilityAttacher(owner, capability, ownerFilter, capFactory);
    }

    /**
     * Enables player's capability transfer upon clone event, so it could persist across death and dimension travelling.
     *
     * @param serializableCapExtractor function which should give access to serializable capability based on provided player.
     */
    public static <T extends Tag> void makePlayerCapTransferOnClone(Function<Player, INBTSerializable<T>> serializableCapExtractor) {
        TimeCore.INSTANCE.getCapabilityManager().makePlayerCapTransferOnClone(serializableCapExtractor);
    }

    /**
     * Enables automatic player capability syncing on such events as:
     * <ol>
     *  <li>{@link EntityJoinLevelEvent}</li>
     *  <li>{@link PlayerChangedDimensionEvent}</li>
     *  <li>{@link PlayerRespawnEvent}</li>
     * </ol>
     *
     * @param syncFunction function, which should send needed capability data to the client upon request
     */
    public static void makePlayerCapSyncOnJoin(Consumer<Player> syncFunction) {
        TimeCore.INSTANCE.getCapabilityManager().makePlayerCapSyncOnJoin(syncFunction);
    }

    /**
     * Enables an automatic entity capability syncing upon {@link StartTracking} event.
     * <br>
     * <b color="yellow">Beware: Capability may be null at this point!</b>
     *
     * @param syncFunction function, which should send needed capability data to the client upon start tracking
     */
    public static void makeEntityCapSyncOnStartTracking(EntityCapSyncOnStartTrackListener.SyncFunction syncFunction) {
        TimeCore.INSTANCE.getCapabilityManager().makeEntityCapSyncOnStartTracking(syncFunction);
    }
}
