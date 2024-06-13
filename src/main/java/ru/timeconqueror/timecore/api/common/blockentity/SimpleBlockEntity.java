package ru.timeconqueror.timecore.api.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

/**
 * Block entity with some useful methods
 */
public abstract class SimpleBlockEntity extends BlockEntity {
    public SimpleBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    public boolean isServerSide() {
        return !isClientSide();
    }

    public boolean isClientSide() {
        Level level = Objects.requireNonNull(getLevel());
        return level.isClientSide();
    }

    /**
     * Returns the blockstate on blockentity pos.
     */
    public BlockState getState() {
        Level level = Objects.requireNonNull(getLevel());

        return level.getBlockState(worldPosition);
    }
}
