package examples.registry_example;

import examples.block.DummyBlockWithEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import ru.timeconqueror.timecore.TimeCore;
import ru.timeconqueror.timecore.api.client.resource.BlockModels;
import ru.timeconqueror.timecore.api.client.resource.location.BlockModelLocation;
import ru.timeconqueror.timecore.api.client.resource.location.TextureLocation;
import ru.timeconqueror.timecore.api.registry.BlockRegister;
import ru.timeconqueror.timecore.api.registry.util.AutoRegistrable;
import ru.timeconqueror.timecore.api.registry.util.BlockPropsFactory;

import static ru.timeconqueror.timecore.api.util.Hacks.promise;

@AutoRegistrable.Entries("block")
public class BlockRegistryExample {
    public static Block TEST_SAND = promise();
    public static Block TEST_DIRT = promise();
    public static Block TEST_EMERALD_ORE = promise();
    public static Block TEST_BLOCK_WITH_TILE = promise();

    private static class Init {
        @AutoRegistrable
        private static final BlockRegister REGISTER = new BlockRegister(TimeCore.MODID);

        @AutoRegistrable.Init
        private static void register() {
            BlockPropsFactory propsCreator = new BlockPropsFactory(Block.Properties::of);

            REGISTER.register("test_sand", () -> new Block(propsCreator.create()))
                    .defaultBlockItem(CreativeModeTabs.TOOLS_AND_UTILITIES)
                    .also(chain -> {
                        BlockModelLocation modelLocation = new BlockModelLocation(chain.getModId(), "block/test_sand");
                        chain
                                .model(modelLocation, () -> BlockModels.cubeAllModel(new TextureLocation("minecraft", "block/sand")))
                                .oneVariantState(modelLocation);
                    });

            REGISTER.register("test_dirt", () -> new Block(propsCreator.create()))
                    .oneVarStateAndCubeAllModel(new TextureLocation("minecraft", "block/dirt"));

            REGISTER.register("test_emerald_ore", () -> new Block(propsCreator.create()))
                    .defaultBlockItem(CreativeModeTabs.TOOLS_AND_UTILITIES)
                    .oneVarStateAndCubeAllModel(new TextureLocation("minecraft", "block/emerald_ore"));

            REGISTER.register("test_block_with_tile", () -> new DummyBlockWithEntity(propsCreator.create()))
                    .defaultBlockItem(CreativeModeTabs.TOOLS_AND_UTILITIES)
                    .oneVarStateAndCubeAllModel(new TextureLocation("minecraft", "block/furnace"));
        }
    }
}
