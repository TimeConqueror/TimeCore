package ru.timeconqueror.timecore.entity;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.World;
import ru.timeconqueror.timecore.api.client.animation.AnimationManager;
import ru.timeconqueror.timecore.api.client.animation.IAnimationProvider;
import ru.timeconqueror.timecore.registry.TEntities;

import javax.annotation.Nullable;

@SuppressWarnings("EntityConstructor")
public class EntityZombie extends EntityStupidAnimal implements IAnimationProvider {
    private AnimationManager animationManager = new AnimationManager();

    public EntityZombie(EntityType<? extends AnimalEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Nullable
    @Override
    public AgeableEntity createChild(AgeableEntity ageable) {
        return new EntityZombie((EntityType<? extends AnimalEntity>) getType(), this.world);
    }

    @Override
    public double getSpeed() {
        return 0.37D;
    }

    public boolean isFemale() {
        return false;
    }

    @Override
    public AnimationManager getAnimationManager() {
        return animationManager;
    }

    @Override
    public void livingTick() {
        super.livingTick();

        if ((System.currentTimeMillis() / 1000) % 5 == 0) {
            animationManager.startAnimationIgnorable(TEntities.HIT_ANIMATION, 333);
//            animationManager.startAnimation(TEntities.SCALING_ANIMATION);
//            animationManager.startAnimation(TEntities.OFFSETTING_ANIMATION, InsertType.IGNORE);
        }
    }
}
