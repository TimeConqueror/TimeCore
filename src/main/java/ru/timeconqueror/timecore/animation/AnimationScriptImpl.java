package ru.timeconqueror.timecore.animation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import ru.timeconqueror.timecore.api.animation.AnimationScript;

@Getter
@AllArgsConstructor
public class AnimationScriptImpl implements AnimationScript {
    private final AnimationData animationData;
    private final AnimationCompanionData companionData;
    private final AnimationScript nextScript;

    public static void encode(AnimationScript animationScript, FriendlyByteBuf buffer) {
        AnimationData animationData = animationScript.getAnimationData();
        AnimationCompanionData companionData = animationScript.getCompanionData();
        AnimationScript nextScript = animationScript.getNextScript();

        AnimationData.encode(animationData, buffer);
        AnimationCompanionData.encode(companionData, buffer);

        boolean hasNext = nextScript != null;
        buffer.writeBoolean(hasNext);
        if (hasNext) {
            encode(nextScript, buffer);
        }
    }

    public static AnimationScript decode(FriendlyByteBuf buffer) {
        AnimationData animationData = AnimationData.decode(buffer);
        AnimationCompanionData companionData = AnimationCompanionData.decode(buffer);
        AnimationScript nextScript = null;

        boolean hasNext = buffer.readBoolean();
        if (hasNext) {
            nextScript = decode(buffer);
        }

        return new AnimationScriptImpl(animationData, companionData, nextScript);
    }
}
