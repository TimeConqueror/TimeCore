package ru.timeconqueror.timecore.api.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Objects;

/**
 * Syncable BlockEntity.
 * <p>
 * You can also control what should be sent to client
 * and what should be only used for saving.
 */
public abstract class SyncableBlockEntity extends SimpleBlockEntity {
    public SyncableBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    /**
     * For saving/sending data use {@link #writeNBT(CompoundTag, SerializationType)}
     */
    @NotNull
    @Override
    public final void saveAdditional(@NotNull CompoundTag compound) {
        writeNBT(compound, SerializationType.SAVE);
    }

    /**
     * For saving/sending data use {@link #readNBT(CompoundTag, SerializationType)}
     */
    @Override
    public final void load(CompoundTag compound) {
        //If read from client side
        if (compound.contains("client_flag")) {
            readNBT(compound, SerializationType.SYNC);
        } else {
            readNBT(compound, SerializationType.SAVE);
        }
    }

    @OverridingMethodsMustInvokeSuper
    protected void writeNBT(CompoundTag nbt, SerializationType type) {
        super.saveAdditional(nbt);
    }

    @OverridingMethodsMustInvokeSuper
    protected void readNBT(CompoundTag nbt, SerializationType type) {
        super.load(nbt);
    }

    @Nonnull
    @Override
    public final CompoundTag getUpdateTag() {
        CompoundTag compound = new CompoundTag();

        writeNBT(compound, SerializationType.SYNC);

        compound.putByte("client_flag", (byte) 0);
        return compound;
    }

    @Override
    public final void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag compound = pkt.getTag();

        readNBT(compound, SerializationType.SYNC);
    }

    @Override
    public final ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Saves current data to the disk and sends update to client.
     */
    public void saveAndSync() {
        if (isServerSide()) {
            setBlockToUpdateAndSave();
        }
    }

    /**
     * Saves current data to the disk without sending update to client.
     */
    public void save() {
        if (isServerSide()) {
            setChanged();
        }
    }

    /**
     * Saves the block to disk, sends packet to update it on client.
     */
    private void setBlockToUpdateAndSave() {
        Objects.requireNonNull(level);

        setChanged();
        level.sendBlockUpdated(worldPosition, getState(), getState(), 2);
    }
}
