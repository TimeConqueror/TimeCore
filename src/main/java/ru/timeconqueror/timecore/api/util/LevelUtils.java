package ru.timeconqueror.timecore.api.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import ru.timeconqueror.timecore.TimeCore;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LevelUtils {
    public static <T> void forTypedBlockEntity(Level world, BlockPos pos, Class<T> clazz, Consumer<T> action) {
        forTypedBlockEntity(world, pos, clazz, action, s -> {
        });
    }

    public static <T> void forTypedBlockEntityWithWarn(Player player, Level world, BlockPos pos, Class<T> clazz, Consumer<T> action) {
        forTypedBlockEntity(world, pos, clazz, action, message -> {
            PlayerUtils.sendMessage(player, Component.literal(message).withStyle(ChatFormatting.RED));
            TimeCore.LOGGER.warn(message, new IllegalAccessException());
        });
    }

    public static <T> void forTypedBlockEntityWithWarn(Level world, BlockPos pos, Class<T> clazz, Consumer<T> action) {
        forTypedBlockEntity(world, pos, clazz, action, message -> TimeCore.LOGGER.warn(message, new IllegalAccessException()));
    }

    public static <T> void forBlockEntityWithReqt(Level world, BlockPos pos, Class<T> clazz, Consumer<T> action) {
        forTypedBlockEntity(world, pos, clazz, action, error -> {
            throw new IllegalStateException(error);
        });
    }

    public static <T> void forTypedBlockEntity(Level world, BlockPos pos, Class<T> clazz, Consumer<T> action, Consumer<String> errorHandler) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity == null) {
            errorHandler.accept("Error. There's no block entity on " + pos);//TODO localize here and in LootGames, TODO "more info in logs, where will be current block"
            return;
        }

        if (clazz.isInstance(blockEntity)) {
            action.accept((T) blockEntity);
        } else {
            errorHandler.accept("Error. There's a block entity " + blockEntity.getClass().getName() + " instead of " + clazz.getName() + " on " + pos);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> makeSimpleTicker(BlockEntityType<A> providedType, BlockEntityType<E> requiredType, BlockEntityTicker<? super E> ticker) {
        return requiredType == providedType ? (BlockEntityTicker<A>) ticker : null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> makeSimpleTicker(BlockEntityType<A> providedType, Supplier<BlockEntityType<E>> requiredType, BlockEntityTicker<? super E> ticker) {
        return requiredType.get() == providedType ? (BlockEntityTicker<A>) ticker : null;
    }
}
