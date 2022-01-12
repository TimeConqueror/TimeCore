package ru.timeconqueror.timecore.api.registry;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegistryObject;
import ru.timeconqueror.timecore.api.TimeCoreAPI;
import ru.timeconqueror.timecore.api.client.render.tile.ProfiledTileEntityRenderer;
import ru.timeconqueror.timecore.api.registry.util.AutoRegistrable;
import ru.timeconqueror.timecore.api.util.Hacks;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * All {@link TimeRegister}s are used to simplify stuff registering.
 * You can use it for both {@link RegistryObject} or {@link ObjectHolder} style.
 * <p>
 * To use it you need to:
 * <ol>
 *     <li>Create its instance and declare it static. Access modifier can be any.</li>
 *     <li>Attach {@link AutoRegistrable} annotation to it to register it as an event listener.</li>
 *     <li>Call {@link TimeCoreAPI#setup(Object)} from your mod constructor to enable TimeCore's annotations.</li>
 * </ol>
 *
 * <b>Features:</b>
 * If you need to register stuff, your first step will be to call method #register.
 * If the register system has any extra available registering stuff, then this method will return Register Chain,
 * which will have extra methods to apply.
 * Otherwise it will RegistryObject, which can be used or not used (depending on your registry style).
 * <br>
 * <br>
 * <b>{@link RegistryObject} style:</b>
 * <br>
 * <blockquote>
 *     <pre>
 *     public class TileEntityDeferredRegistryExample {
 *         {@literal @}AutoRegistrable
 *          private static final TileEntityRegister REGISTER = new TileEntityRegister(TimeCore.MODID);
 *
 *          public static RegistryObject<TileEntityType<DummyTileEntity>> TEST_TE_TYPE = REGISTER.register("test_tile", DummyTileEntity::new, BlockRegistryExample.TEST_BLOCK_WITH_TILE)
 *              .regCustomRenderer(() -> DummyTileEntityRenderer::new) // <- one of extra features
 *              .asRegistryObject(); // <- retrieving registry object from our register chain.
 *      }
 *     </pre>
 * </blockquote>
 * <br>
 * <b>{@link ObjectHolder} style:</b>
 * <br>
 * For this style you need to know one thing:
 * you will need two classes: one for storing registry values and one for registering them.
 * In the following case I made registering class an inner class of storing class.
 * If you want, you may store them in separate files, there's no matter.
 * <p>
 * So the storing (main) class needs to have {@link ObjectHolder} annotation with your mod id to inject values in all public static final fields.
 * The name of the field should match it's registry name (ignoring case).
 * More about it you can check in (<a href=https://mcforge.readthedocs.io/en/1.16.x/>Forge Documentation</a>)
 * <p>
 * The inner class will be used for us as a registrator. It should be static, but can have any access modifier.
 * We still add {@link TimeRegister} there as stated above. (with AutoRegistrable annotation, etc.)]
 * <p>
 * One more thing: we should add is a <b>static</b> register method and annotate with {@link AutoRegistrable.Init}. Method can have any access modifier.
 * There we will register all needed stuff, using {@link TimeRegister} field.
 * Method annotated with {@link AutoRegistrable.Init} can have zero parameters or one {@link FMLConstructModEvent} parameter.
 * It will be called before Registry events to prepare all the stuff.
 * <p>
 * As you can see, I used {@link Hacks#promise()} method for public static final fields that will be initialized later.
 * You can place there null, but some IDE may always tell you, that it expects the NullPointerException in all places, where you call it.
 * We know, that it will be initialized later, so using {@link Hacks#promise()} we set null in this field, but disables IDE null checks for it.
 *
 * <br>
 * <blockquote>
 *     <pre>
 *     {@literal @}ObjectHolder(TimeCore.MODID)
 *      public class ItemRegistryExample {
 *          public static final Item TEST_DIAMOND = Hacks.promise();
 *
 *          private static class Init {
 *             {@literal @}AutoRegistrable
 *              private static final ItemRegister REGISTER = new ItemRegister(TimeCore.MODID);
 *
 *             {@literal @}AutoRegistrable.InitMethod
 *              private static void register() {
 *                  ItemPropsFactory miscGrouped = new ItemPropsFactory(ItemGroup.TAB_MISC);
 *
 *                  REGISTER.register("test_diamond", () -> new Item(miscGrouped.create()))
 *                          .genDefaultModel(new TextureLocation("minecraft", "item/diamond"));
 *               }
 *          }
 *      }
 *     </pre>
 * </blockquote>
 * <p>
 * <p>
 * Examples can be seen at test module.
 */
public class TileEntityRegister extends ForgeRegister<BlockEntityType<?>> {
    public TileEntityRegister(String modid) {
        super(ForgeRegistries.BLOCK_ENTITIES, modid);
    }

    /**
     * Adds entry in provided {@code entrySup} to the queue, all entries from which will be registered later.
     * <p>
     * This method also returns {@link TileEntityRegisterChain} to provide extra methods, which you can apply to entry being registered.
     * All method of {@link TileEntityRegisterChain} are optional.
     *
     * @param name              The tile type's name, will automatically have the modid as a namespace.
     * @param tileEntityFactory A factory for the new tile, it should return a new instance every time it is called.
     * @param validBlock        block, which can have this tile type.
     * @return A {@link TileEntityRegisterChain} for adding some extra stuff.
     * @see TileEntityRegisterChain
     */
    public <T extends BlockEntity> TileEntityRegisterChain<T> registerSingleBound(String name, BlockEntityType.BlockEntitySupplier<T> tileEntityFactory, Supplier<Block> validBlock) {
        return register(name, tileEntityFactory, () -> Lists.newArrayList(validBlock.get()));
    }

    /**
     * Adds entry in provided {@code entrySup} to the queue, all entries from which will be registered later.
     * <p>
     * This method also returns {@link TileEntityRegisterChain} to provide extra methods, which you can apply to entry being registered.
     * All methods of {@link TileEntityRegisterChain} are optional.
     *
     * @param name              The tile type's name, will automatically have the modid as a namespace.
     * @param tileEntityFactory A factory for the new tile, it should return a new instance every time it is called.
     * @param validBlocks       blocks, which can have this tile type.
     * @return A {@link TileEntityRegisterChain} for adding some extra stuff.
     * @see TileEntityRegisterChain
     */
    public <T extends BlockEntity> TileEntityRegisterChain<T> register(String name, BlockEntityType.BlockEntitySupplier<T> tileEntityFactory, Supplier<List<Block>> validBlocks) {
        Supplier<BlockEntityType<T>> typeSupplier = () ->
                BlockEntityType.Builder.of(tileEntityFactory, validBlocks.get().toArray(new Block[0]))
                        .build(null /*forge doesn't have support for it*/);

        RegistryObject<BlockEntityType<T>> holder = registerEntry(name, typeSupplier);
        return new TileEntityRegisterChain<>(holder);
    }

    public class TileEntityRegisterChain<T extends BlockEntity> extends RegisterChain<BlockEntityType<T>> {
        private TileEntityRegisterChain(RegistryObject<BlockEntityType<T>> holder) {
            super(holder);
        }

        public TileEntityRegisterChain<T> regCustomRenderer(Supplier<Function<? super BlockEntityRendererProvider.Context, BlockEntityRenderer<? super T>>> rendererFactory) {
            clientSideOnly(() -> TileEntityRegister.regCustomRenderer(TileEntityRegister.this, asRegistryObject(), rendererFactory));
            return this;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static <T extends BlockEntity> void regCustomRenderer(TileEntityRegister register, RegistryObject<BlockEntityType<T>> registryObject, Supplier<Function<? super BlockEntityRendererProvider.Context, BlockEntityRenderer<? super T>>> rendererFactory) {
        register.runOnClientSetup(() -> {
            BlockEntityRenderers.register(registryObject.get(), context_ -> new ProfiledTileEntityRenderer<>(context_, rendererFactory.get()));
        });
    }
}
