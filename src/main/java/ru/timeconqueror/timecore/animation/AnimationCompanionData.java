package ru.timeconqueror.timecore.animation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import ru.timeconqueror.timecore.animation.action.PredefinedActionManagerImpl;
import ru.timeconqueror.timecore.api.animation.action.ActionInstance;
import ru.timeconqueror.timecore.api.util.BufferUtils;

import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
public class AnimationCompanionData {
    public static final AnimationCompanionData EMPTY = new AnimationCompanionData(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

    /**
     * Actions, which can be registered in {@link PredefinedActionManagerImpl} and then run by its id.
     * Synced.
     */
    private final List<String> predefinedSyncedActions;
    /**
     * Actions, which can be registered in {@link PredefinedActionManagerImpl} and then run by its id.
     * Can't be synced.
     */
    private transient final List<String> predefinedUnsyncedActions;
    /**
     * Actions, which will be played only on the side, where the animation script is created.
     * Can't be synced.
     */
    private transient final List<ActionInstance<?, ?>> inplaceActions;

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public static void encode(AnimationCompanionData companionData, FriendlyByteBuf buf) {
        buf.writeBoolean(companionData == EMPTY);
        BufferUtils.encodeStringList(companionData.predefinedSyncedActions, buf);
    }

    public static AnimationCompanionData decode(FriendlyByteBuf buf) {
        boolean empty = buf.readBoolean();
        if (empty) {
            return AnimationCompanionData.EMPTY;
        }

        List<String> predefinedSyncedActions = BufferUtils.decodeStringList(buf);
        return new AnimationCompanionData(predefinedSyncedActions, Collections.emptyList(), Collections.emptyList());
    }
}
