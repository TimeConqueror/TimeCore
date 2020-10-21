package ru.timeconqueror.timecore.registry.common;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import ru.timeconqueror.timecore.registry.TimeAutoRegistrable;
import ru.timeconqueror.timecore.registry.common.base.WrappedForgeTimeRegistry;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Used for simplifying tile entity adding. You need to extend it and do your stuff in {@link #register()} method<br>
 * <p>
 * Any your registry that extends it should be annotated by {@link TimeAutoRegistrable} with {@link TimeAutoRegistrable.Target#INSTANCE} target
 * to create its instance automatically and provide register features.<br>
 *
 * <b><font color="yellow">WARNING: Any annotated registry class must contain constructor without params or exception will be thrown.</b><br>
 * Examples can be seen at test module.
 */
public abstract class TileEntityTimeRegistry extends WrappedForgeTimeRegistry<TileEntityType<?>> {
    private ArrayList<Supplier<Runnable>> rendererRegisterRunnables = new ArrayList<>();

    @SubscribeEvent
    public final void onRegTileEntitiesEvent(RegistryEvent.Register<TileEntityType<?>> event) {
        onFireRegistryEvent(event);
    }

    @SubscribeEvent
    public final void onClientSetupEvent(FMLClientSetupEvent event) {
        for (Supplier<Runnable> runnable : rendererRegisterRunnables) {
            runnable.get().run();
        }

        rendererRegisterRunnables.clear();
        rendererRegisterRunnables = null;
    }

    /**
     * Method to register tileEntities automatically.
     *
     * @param tileEntitySupplier supplier, that returns new TileEntity objects.
     * @param name               tile entity location.
     *                           It will be used as a part of registry key. Should NOT contain mod ID, because it will be bound automatically.
     * @param validBlocks        blocks, that can contain provided tile entity.
     * @return {@link TileEntityWrapper} to provide extra register options.
     */
    public <T extends TileEntity> TileEntityWrapper<T> regTileEntity(Supplier<T> tileEntitySupplier, String name, Block... validBlocks) {
        TileEntityType<T> type = TileEntityType.Builder.of(tileEntitySupplier, validBlocks).build(null);
        return new TileEntityWrapper<>(type, name);
    }

    /**
     * Method to register renderers for tileEntities.
     *
     * @param tileEntityType   tile entity type, for which you want to apply special renderer.
     * @param rendererSupplier supplier, that should return instance of {@link TileEntityRenderer}.
     *                         Here we use supplier to hide from java client classes.
     *                         If we don't do it, then it will crash on server side.
     * @param <T>              any class inherited from TileEntity.
     */
    public <T extends TileEntity> void regTileEntityRenderer(TileEntityType<T> tileEntityType, Supplier<Function<? super TileEntityRendererDispatcher, ? extends TileEntityRenderer<? super T>>> rendererSupplier) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            Supplier<Runnable> runnable = () -> () -> ClientRegistry.bindTileEntityRenderer(tileEntityType, rendererSupplier.get());
            rendererRegisterRunnables.add(runnable);
        }
    }

    public class TileEntityWrapper<T extends TileEntity> extends EntryWrapper {
        public TileEntityWrapper(TileEntityType<T> entry, String name) {
            super(entry, name);
        }

        /**
         * Returns tile entity type bound to wrapper.
         * Method duplicates {@link #getEntry()}, so it exists only for easier understanding.
         */
        @SuppressWarnings("unchecked")
        public TileEntityType<T> retrieveTileEntityType() {
            return (TileEntityType<T>) getEntry();
        }

        /**
         * Method to register renderer for provided tile entity.
         *
         * @param rendererSupplier supplier, that should return instance of {@link TileEntityRenderer}.
         *                         Here we use supplier to hide from java client classes.
         *                         If we don't do it, then it will crash on server side.
         */
        public TileEntityWrapper<T> regCustomRenderer(Supplier<Function<? super TileEntityRendererDispatcher, ? extends TileEntityRenderer<? super T>>> rendererSupplier) {
            regTileEntityRenderer(retrieveTileEntityType(), rendererSupplier);

            return this;
        }
    }
}
