package ru.timeconqueror.timecore.animation;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.timeconqueror.timecore.api.animation.action.BakedAction;
import ru.timeconqueror.timecore.test_utils.CartesianProduct;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AnimationCompanionDataTest {

    @ParameterizedTest
    @MethodSource("companionDataVariations")
    public void testEncodedSameAsDecoded(AnimationCompanionData data) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        AnimationCompanionData.encode(data, buffer);
        AnimationCompanionData decoded = AnimationCompanionData.decode(buffer);

        assertEquals(Collections.emptyList(), decoded.getInplaceActions());
        assertEquals(Collections.emptyList(), decoded.getPredefinedActionsToSend());
        assertEquals(data.getPredefinedActionsToSend(), decoded.getPredefinedActionsToPlay());
    }

    @Test
    public void testEncodedEmpty() {
        AnimationCompanionData data = AnimationCompanionData.EMPTY;
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        AnimationCompanionData.encode(data, buffer);
        AnimationCompanionData decoded = AnimationCompanionData.decode(buffer);
        assertEquals(AnimationCompanionData.EMPTY, decoded);
    }

    public static List<AnimationCompanionData> companionDataVariations() {
        List<List<String>> actionsToSend = List.of(
                Collections.emptyList(), List.of("test1")
        );
        List<List<String>> actionsToPlay = List.of(
                Collections.emptyList(), List.of("test2")
        );

        BakedAction<?> mockedActionInstance = mock(BakedAction.class);
        List<List<BakedAction<?>>> inplaceActions = List.of(
                Collections.emptyList(), List.of(mockedActionInstance)
        );

        return CartesianProduct.ofThree(actionsToSend, actionsToPlay, inplaceActions,
                AnimationCompanionData::new);
    }
}