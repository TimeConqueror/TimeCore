package ru.timeconqueror.timecore.mod.mixins;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.timeconqueror.timecore.common.TimeEventHooks;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void onLivingTickEnd(CallbackInfo ci) {
        TimeEventHooks.onLivingUpdateEnd((LivingEntity) (Object) this);
    }
}
