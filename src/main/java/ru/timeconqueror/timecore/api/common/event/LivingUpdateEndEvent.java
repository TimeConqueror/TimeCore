package ru.timeconqueror.timecore.api.common.event;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

/**
 * Called at the end of {@link LivingEntity#tick()}.
 * It exists, because {@link LivingUpdateEvent} is posted at the tick start, where {@code prevPos} is the same as {@code prevPos}.
 * <p>
 * This event is not {@link Cancelable}.<br>
 * <p>
 * This event does not have a result. {@link HasResult}<br>
 * <p>
 * Posted by {@link Mod.EventBusSubscriber.Bus#FORGE} event bus.
 */
public class LivingUpdateEndEvent extends LivingEvent {
    private final LogicalSide dist;

    public LivingUpdateEndEvent(LivingEntity entity) {
        super(entity);

        dist = entity.world.isRemote ? LogicalSide.CLIENT : LogicalSide.SERVER;
    }

    public LogicalSide getLogicalSide() {
        return dist;
    }
}
