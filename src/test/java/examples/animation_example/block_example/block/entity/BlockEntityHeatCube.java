package examples.animation_example.block_example.block.entity;

import examples.animation_example.block_example.registry.ABlockEntityRegistry;
import examples.animation_example.block_example.registry.BlockEntityAnimations;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ru.timeconqueror.timecore.animation.AnimationSystem;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.animation.AnimationConstants;
import ru.timeconqueror.timecore.api.animation.AnimationStarter;
import ru.timeconqueror.timecore.api.animation.AnimationSystems;
import ru.timeconqueror.timecore.api.util.ITickableBlockEntity;

public class BlockEntityHeatCube extends BlockEntity implements AnimatedObject<BlockEntityHeatCube>, ITickableBlockEntity {
    private AnimationSystem<BlockEntityHeatCube> animationSystem;

    public BlockEntityHeatCube(BlockPos worldPosition_, BlockState blockState_) {
        super(ABlockEntityRegistry.HEAT_CUBE, worldPosition_, blockState_);
    }

    @Override
    public void onLoad() {
        animationSystem = AnimationSystems.forBlockEntity(this, animationManagerBuilder -> {
        });
    }

    @Override
    public AnimationSystem<BlockEntityHeatCube> animationSystem() {
        return animationSystem;
    }

    @Override
    public void tick(Level level) {
        animationSystem().onTick(level.isClientSide);

        if (level.isClientSide) {
            animationSystem.startAnimation(AnimationStarter.of(BlockEntityAnimations.heatCubeIdle).ignorable(true), AnimationConstants.MAIN_LAYER_NAME);
        }
    }
}
