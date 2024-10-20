package ru.timeconqueror.timecore.animation;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.timeconqueror.timecore.animation.component.LoopMode;
import ru.timeconqueror.timecore.api.animation.Animation;
import ru.timeconqueror.timecore.api.animation.AnimationStarter;
import ru.timeconqueror.timecore.api.reflection.ReflectionHelper;

import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AnimationDataTest {

    private Animation animation;

    @BeforeEach
    public void setup() {
        animation = mock(Animation.class);
        doReturn(new ResourceLocation("test")).when(animation).getId();
        AnimationRegistry.registerAnimation(animation);
    }

    @AfterEach
    public void clear() {
        ReflectionHelper.<AnimationRegistry, Map<ResourceLocation, Animation>>findField(AnimationRegistry.class, "animationMap")
                .get(null)
                .clear();
    }

    @ParameterizedTest
    @EnumSource(LoopMode.class)
    @NullSource
    public void testEncodedSameAsDecoded(LoopMode loopMode) {
        var data = AnimationStarter.of(animation)
                .ignorable(true)
                .startingFrom(10)
                .withTransitionTime(1000)
                .withSpeed(2)
                .withNoTransitionToNone()
                .reversed(true)
                .withLoopMode(loopMode)
                .getData();

        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        AnimationData.encode(data, buffer);
        AnimationData realData = AnimationData.decode(buffer);
        Assertions.assertEquals(data, realData);
    }
}