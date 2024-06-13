package examples.block;

import examples.registry_example.deferred.BlockEntityDeferredRegistryExample;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class DummyBlockWithEntity extends BaseEntityBlock {
    public DummyBlockWithEntity(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos_, BlockState state_) {
        return BlockEntityDeferredRegistryExample.TEST_TE_TYPE.get().create(pos_, state_);
    }
}
