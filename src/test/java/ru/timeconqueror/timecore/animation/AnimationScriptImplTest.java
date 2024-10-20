package ru.timeconqueror.timecore.animation;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.timeconqueror.timecore.api.animation.Animation;
import ru.timeconqueror.timecore.api.animation.AnimationScript;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class AnimationScriptImplTest {
    private AnimationCompanionData companion = AnimationCompanionData.EMPTY;

    @BeforeEach
    public void setup() {
    }

    @Test
    public void testEncodedSameAsDecodedWithNullNextScript() {
        Animation animation = mockAndSetupAnimation("test");
        AnimationScriptImpl script = new AnimationScriptImpl(new AnimationStarterImpl(animation).getData(), AnimationCompanionData.EMPTY, null);

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        AnimationScriptImpl.encode(script, buffer);
        AnimationScript decoded = AnimationScriptImpl.decode(buffer);

        assertEquals(script.getAnimationData(), decoded.getAnimationData());
        assertEquals(script.getCompanionData(), decoded.getCompanionData());
        assertNull(decoded.getNextScript());
    }

    @Test
    public void testEncodedSameAsDecoded() {
        Animation animation2 = mockAndSetupAnimation("test2");
        AnimationScriptImpl script2 = new AnimationScriptImpl(new AnimationStarterImpl(animation2).getData(), AnimationCompanionData.EMPTY, null);
        Animation animation1 = mockAndSetupAnimation("test1");
        AnimationScriptImpl script = new AnimationScriptImpl(new AnimationStarterImpl(animation1).getData(), AnimationCompanionData.EMPTY, script2);

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        AnimationScriptImpl.encode(script, buffer);
        AnimationScript decoded = AnimationScriptImpl.decode(buffer);

        assertEquals(script.getAnimationData(), decoded.getAnimationData());
        assertEquals(script.getCompanionData(), decoded.getCompanionData());

        AnimationScript nextScript = script.getNextScript();
        assertNotNull(nextScript);
        assertNotNull(decoded.getNextScript());
        assertEquals(nextScript.getAnimationData(), decoded.getNextScript().getAnimationData());
        assertEquals(nextScript.getCompanionData(), decoded.getNextScript().getCompanionData());
    }

    private Animation mockAndSetupAnimation(String id) {
        Animation animation = mock(Animation.class);
        doReturn(new ResourceLocation(id)).when(animation).getId();
        return AnimationRegistry.registerAnimation(animation);
    }
}