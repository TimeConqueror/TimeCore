package ru.timeconqueror.timecore.api.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BufferUtils {
    public static void encodeBoundingBox(AABB boundingBox, FriendlyByteBuf bufferTo) {
        bufferTo.writeDouble(boundingBox.minX);
        bufferTo.writeDouble(boundingBox.minY);
        bufferTo.writeDouble(boundingBox.minZ);
        bufferTo.writeDouble(boundingBox.maxX);
        bufferTo.writeDouble(boundingBox.maxY);
        bufferTo.writeDouble(boundingBox.maxZ);
    }

    public static AABB decodeBoundingBox(FriendlyByteBuf bufferFrom) {
        return new AABB(
                bufferFrom.readDouble(),
                bufferFrom.readDouble(),
                bufferFrom.readDouble(),
                bufferFrom.readDouble(),
                bufferFrom.readDouble(),
                bufferFrom.readDouble()
        );
    }

    public static void encodeStringList(List<String> strings, FriendlyByteBuf bufferTo) {
        bufferTo.writeVarInt(strings.size());
        for (String str : strings) {
            bufferTo.writeUtf(str);
        }
    }

    public static List<String> decodeStringList(FriendlyByteBuf bufferFrom) {
        int size = bufferFrom.readVarInt();
        if (size == 0) {
            return Collections.emptyList();
        }

        List<String> strings = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            strings.add(bufferFrom.readUtf());
        }
        return strings;
    }
}
