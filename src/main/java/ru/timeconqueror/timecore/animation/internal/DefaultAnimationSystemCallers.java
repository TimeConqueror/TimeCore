package ru.timeconqueror.timecore.animation.internal;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.timeconqueror.timecore.animation.BaseAnimationManager;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.animation.AnimationManager;
import ru.timeconqueror.timecore.api.common.event.LivingTickEndEvent;

//TODO add tickers for tile entities
@Mod.EventBusSubscriber
public class DefaultAnimationSystemCallers {
    public static void onEntityTickEnd(LivingTickEndEvent event) {
        LivingEntity living = event.getEntity();

        if (living instanceof AnimatedObject<?> animated) {
            //needed for animation ticking on server side.
            animated.animationSystem().onTick(living.level().isClientSide);
        }
    }

    @SubscribeEvent
    public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        Entity target = event.getTarget();
        if (target instanceof AnimatedObject<?> animatedObj) {
            AnimationManager animationManager = animatedObj.animationSystem().getAnimationManager();
            var statesByLayer = ((BaseAnimationManager) animationManager).getLayerStates();
            animatedObj.animationSystem().getNetworkDispatcher().sendSyncAnimationsPacket(statesByLayer);
        }
    }
}
