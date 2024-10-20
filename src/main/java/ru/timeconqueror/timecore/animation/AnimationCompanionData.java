package ru.timeconqueror.timecore.animation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.network.FriendlyByteBuf;
import ru.timeconqueror.timecore.animation.action.PredefinedActionManagerImpl;
import ru.timeconqueror.timecore.api.animation.action.ActionInstance;
import ru.timeconqueror.timecore.api.util.BufferUtils;

import java.util.Collections;
import java.util.List;

@Getter
@ToString
@AllArgsConstructor
public class AnimationCompanionData {
    public static final AnimationCompanionData EMPTY = new AnimationCompanionData(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

    /**
     * Actions, which can be registered in {@link PredefinedActionManagerImpl} and then be sent to client.
     */
    private final List<String> predefinedActionsToSend;
    /**
     * Actions, which can be registered in {@link PredefinedActionManagerImpl} and then run by its id.
     */
    private transient final List<String> predefinedActionsToPlay;
    /**
     * Actions, which will be played only on the side, where the animation script is created.
     * Can't be synced.
     */
    private transient final List<ActionInstance<?, ?>> inplaceActions;

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public static void encode(AnimationCompanionData companionData, FriendlyByteBuf buf) {
        boolean empty = companionData == EMPTY;
        buf.writeBoolean(empty);
        if (!empty) {
            BufferUtils.encodeStringList(companionData.predefinedActionsToSend, buf);
        }
    }

    public static AnimationCompanionData decode(FriendlyByteBuf buf) {
        boolean empty = buf.readBoolean();
        if (empty) {
            return AnimationCompanionData.EMPTY;
        }

        // it was encoded from server to be played on client, that's why it's not #predefinedActionsToSend anymore
        List<String> predefinedActionsToPlay = BufferUtils.decodeStringList(buf);
        return new AnimationCompanionData(Collections.emptyList(), predefinedActionsToPlay, Collections.emptyList());
    }
}
