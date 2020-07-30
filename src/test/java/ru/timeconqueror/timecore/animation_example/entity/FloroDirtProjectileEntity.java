package ru.timeconqueror.timecore.animation_example.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import ru.timeconqueror.timecore.animation_example.registry.EntityRegistry;

public class FloroDirtProjectileEntity extends DamagingProjectileEntity implements IRendersAsItem {
    @ApiStatus.Internal
    public FloroDirtProjectileEntity(EntityType<FloroDirtProjectileEntity> type, World world) {
        super(type, world);
    }

    public FloroDirtProjectileEntity(World world, LivingEntity thrower, float damage) {
        super(EntityRegistry.FLORO_PROJECTILE_TYPE, world, thrower, damage);
    }

    @Override
    protected float getGravityVelocity() {
        return 0.032F;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.SNOWBALL);
    }
}