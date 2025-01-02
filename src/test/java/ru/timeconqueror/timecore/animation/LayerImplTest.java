package ru.timeconqueror.timecore.animation;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.timeconqueror.timecore.animation.component.BasicAnimation;
import ru.timeconqueror.timecore.animation.component.LoopMode;
import ru.timeconqueror.timecore.animation.watcher.AnimationTickerImpl;
import ru.timeconqueror.timecore.api.animation.Animation;
import ru.timeconqueror.timecore.api.animation.AnimationScript;
import ru.timeconqueror.timecore.api.animation.BlendType;
import ru.timeconqueror.timecore.api.animation.builders.LayerDefinition;
import ru.timeconqueror.timecore.molang.SharedMolangObject;

import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LayerImplTest {
    private static final int ANIMATION_LENGTH = 1000;
    private Animation animation;

    private LayerImpl basicLayer;
    private BaseAnimationManager baseAnimationManager;

    @BeforeEach
    public void setup() {
        var sharedMolangObject = new SharedMolangObject();
        baseAnimationManager = mock(BaseAnimationManager.class);
        when(baseAnimationManager.getSharedMolangObjects()).thenReturn(sharedMolangObject);

        var layer = new LayerImpl(baseAnimationManager, new LayerDefinition("test", BlendType.ADD, 1.0F));
        basicLayer = spy(layer);

        animation = new BasicAnimation(LoopMode.DO_NOT_LOOP, new ResourceLocation("namespace", "path"), "test", ANIMATION_LENGTH, Map.of());
    }

    @Test
    public void shouldStartNewAnimationInstantlyIfTransitionTimeIsZero() {
        AnimationData data = new AnimationStarterImpl(animation).withTransitionTime(0).getData();

        AnimationScript animationScript = mock(AnimationScript.class);
        doReturn(data).when(animationScript).getAnimationData();
        basicLayer.startAnimationScript(animationScript, 0);
        verify(basicLayer).setCurrentTicker(argThat(argument -> argument instanceof AnimationTickerImpl && ((AnimationTickerImpl) argument).getAnimationScript() == animationScript), anyLong());
    }
}
