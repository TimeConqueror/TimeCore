package examples.block.blockentity;

import examples.registry_example.deferred.BlockEntityDeferredRegistryExample;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DummyBlockEntity extends BlockEntity {
    public DummyBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityDeferredRegistryExample.TEST_TE_TYPE.get(), pos, state);

        System.out.println("Me placed");

        System.out.println(getType());
    }
}
