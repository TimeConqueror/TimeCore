package ru.timeconqueror.timecore.registry;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import ru.timeconqueror.timecore.TimeCore;
import ru.timeconqueror.timecore.animation.AnimationRegistry;
import ru.timeconqueror.timecore.animation.loading.AnimationLoader;
import ru.timeconqueror.timecore.api.animation.Animation;
import ru.timeconqueror.timecore.client.render.model.TimeEntityModel;
import ru.timeconqueror.timecore.entity.EntityFloro;
import ru.timeconqueror.timecore.entity.EntityZombie;

@TimeAutoRegistrable(target = TimeAutoRegistrable.Target.CLASS)
public class EntityCommonRegistryExample {
    public static final EntityType<? extends AnimalEntity> ZOMBIE_TYPE = EntityType.Builder.create(EntityZombie::new, EntityClassification.CREATURE)
            .setTrackingRange(80)
            .setShouldReceiveVelocityUpdates(true)
            .size(1, 2)
            .build(TimeCore.MODID + ":zombie");
    public static final EntityType<? extends MonsterEntity> FLORO = EntityType.Builder.create(EntityFloro::new, EntityClassification.MONSTER)
            .setTrackingRange(80)
            .setShouldReceiveVelocityUpdates(true)
            .size(1, 2)
            .build(TimeCore.MODID + ":floro");
    public static TimeEntityModel<EntityZombie> zombieModel;
    public static TimeEntityModel<EntityFloro> floroModel;
    public static Animation HIT_ANIMATION;
    public static Animation SCALING_ANIMATION;
    public static Animation OFFSETTING_ANIMATION;
    //    public static Animation FLORO_HIDING;
    public static Animation FLORO_SHOOT;
    public static Animation FLORO_SHOWING;
    public static Animation FLORO_WALK;

    @SubscribeEvent
    public static void register(RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().register(ZOMBIE_TYPE.setRegistryName(TimeCore.MODID + ":zombie"));
        event.getRegistry().register(FLORO.setRegistryName(TimeCore.MODID + ":floro"));
    }

    @SubscribeEvent
    public static void registerSpawnEggs(final RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                new SpawnEggItem(ZOMBIE_TYPE, 0xFF00FF00, 0xFF000000, new Item.Properties().group(ItemGroup.MISC)).setRegistryName(TimeCore.MODID, "spawn_zombie"),
                new SpawnEggItem(FLORO, 0xFF00FF00, 0xFF000000, new Item.Properties().group(ItemGroup.MISC)).setRegistryName(TimeCore.MODID, "spawn_floro")
        );
    }

    @SubscribeEvent
    public static void registerAnimations(FMLCommonSetupEvent event) {
//        FLORO_HIDING = TimeClientLoader.loadAnimations(new ResourceLocation(TimeCore.MODID, "animations/floro.hiding.json")).get(0);
        HIT_ANIMATION = AnimationRegistry.registerAnimation(AnimationLoader.loadAnimation(new ResourceLocation(TimeCore.MODID, "animations/zombie_hit.json")));
        SCALING_ANIMATION = AnimationRegistry.registerAnimation(AnimationLoader.loadAnimation(new ResourceLocation(TimeCore.MODID, "animations/scaling.json")));
        OFFSETTING_ANIMATION = AnimationRegistry.registerAnimation(AnimationLoader.loadAnimation(new ResourceLocation(TimeCore.MODID, "animations/offsetting.json")));
        FLORO_SHOOT = AnimationRegistry.registerAnimation(AnimationLoader.loadAnimation(new ResourceLocation(TimeCore.MODID, "animations/floro.shoot.json")));
        FLORO_SHOWING = AnimationRegistry.registerAnimation(AnimationLoader.loadAnimation(new ResourceLocation(TimeCore.MODID, "animations/floro.showing.json")));
        FLORO_WALK = AnimationRegistry.registerAnimation(AnimationLoader.loadAnimation(new ResourceLocation(TimeCore.MODID, "animations/floro.walk.json")));
    }

    @SubscribeEvent
    public static void registerRenders(FMLClientSetupEvent event) {
//        zombieModel = TimeModelLoader.loadJsonEntityModel(new ResourceLocation(TimeCore.MODID, "models/entity/zombie.json"));//FIXME
//        floroModel = TimeModelLoader.loadJsonEntityModel(new ResourceLocation(TimeCore.MODID, "models/entity/floro.json"));

//        RenderingRegistry.registerEntityRenderingHandler(EntityZombie.class, RenderZombie::new);//FIXME
//        RenderingRegistry.registerEntityRenderingHandler(EntityFloro.class, RenderFloro::new);//FIXME
    }
}