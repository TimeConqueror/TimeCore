package ru.timeconqueror.timecore.api.util;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class Utils {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Registry<T> getRegistry(ResourceKey<Registry<T>> registryKey) {
       return (Registry<T>) BuiltInRegistries.REGISTRY.get((ResourceKey)registryKey);//FIXME check
    }

    public static ResourceLocation getKey(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block);
    }

    public static ResourceLocation getKey(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }
}
