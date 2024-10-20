package ru.timeconqueror.timecore.api.util;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BufferUtilsTest {
    @ParameterizedTest
    @MethodSource("stringListVariations")
    public void testStringListEncodedSameAsDecoded(List<String> encoded) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        BufferUtils.encodeStringList(encoded, buffer);
        List<String> decoded = BufferUtils.decodeStringList(buffer);

        assertEquals(encoded.size(), decoded.size());
        for (int i = 0; i < encoded.size(); i++) {
            String s = encoded.get(i);
            assertEquals(s, decoded.get(i));
        }
    }

    public static Stream<List<String>> stringListVariations() {
        return Stream.of(
                List.of(),
                List.of("test"),
                List.of("test1", "test2")
        );
    }
}