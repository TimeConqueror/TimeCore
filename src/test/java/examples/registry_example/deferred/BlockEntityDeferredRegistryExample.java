package examples.registry_example.deferred;

import examples.block.blockentity.DummyBlockEntity;
import examples.client.DummyBlockEntityRenderer;
import examples.registry_example.BlockRegistryExample;
import net.minecraft.world.level.block.entity.BlockEntityType;
import ru.timeconqueror.timecore.TimeCore;
import ru.timeconqueror.timecore.api.registry.BlockEntityRegister;
import ru.timeconqueror.timecore.api.registry.util.AutoRegistrable;
import ru.timeconqueror.timecore.api.registry.util.Promised;

public class BlockEntityDeferredRegistryExample {
    @AutoRegistrable
    private static final BlockEntityRegister REGISTER = new BlockEntityRegister(TimeCore.MODID);

    public static Promised<BlockEntityType<DummyBlockEntity>> TEST_TE_TYPE = REGISTER.registerSingleBound("test_tile", DummyBlockEntity::new, () -> BlockRegistryExample.TEST_BLOCK_WITH_TILE)
            .regCustomRenderer(() -> DummyBlockEntityRenderer::new)
            .asPromised();
}
