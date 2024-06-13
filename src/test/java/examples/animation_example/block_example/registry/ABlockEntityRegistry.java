package examples.animation_example.block_example.registry;

import examples.animation_example.block_example.block.entity.BlockEntityHeatCube;
import examples.animation_example.block_example.client.render.BERHeatCube;
import net.minecraft.world.level.block.entity.BlockEntityType;
import ru.timeconqueror.timecore.TimeCore;
import ru.timeconqueror.timecore.api.registry.BlockEntityRegister;
import ru.timeconqueror.timecore.api.registry.util.AutoRegistrable;

import static ru.timeconqueror.timecore.api.util.Hacks.promise;

@AutoRegistrable.Entries("block_entity_type")
public class ABlockEntityRegistry {
    public static BlockEntityType<BlockEntityHeatCube> HEAT_CUBE = promise();

    private static class Init {
        @AutoRegistrable
        private static final BlockEntityRegister REGISTER = new BlockEntityRegister(TimeCore.MODID);

        @AutoRegistrable.Init
        private static void register() {
            REGISTER.registerSingleBound("heat_cube", BlockEntityHeatCube::new, () -> ABlockRegistry.HEAT_CUBE)
                    .regCustomRenderer(() -> BERHeatCube::new);
        }
    }
}
