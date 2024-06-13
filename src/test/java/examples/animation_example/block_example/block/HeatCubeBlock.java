package examples.animation_example.block_example.block;

import examples.animation_example.block_example.registry.ABlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import ru.timeconqueror.timecore.api.util.ITickableBlockEntity;

import javax.annotation.Nullable;

public class HeatCubeBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 12, 14);

    public HeatCubeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state_, BlockGetter level_, BlockPos pos_, CollisionContext context_) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state_) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos_, BlockState state_) {
        return ABlockEntityRegistry.HEAT_CUBE.create(pos_, state_);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level_, BlockState state_, BlockEntityType<T> blockEntityType_) {
        return ITickableBlockEntity.makeTicker(blockEntityType_, ABlockEntityRegistry.HEAT_CUBE);
    }
}
