package examples.visual_tests.animation.entity.entity;

import examples.visual_tests.animation.entity.registry.AnimTestEntityAnimations;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import ru.timeconqueror.timecore.animation.AnimationSystem;
import ru.timeconqueror.timecore.animation.predefined.EntityPredefinedAnimations;
import ru.timeconqueror.timecore.animation.predefined.PredefinedAnimation;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.animation.AnimationSystems;
import ru.timeconqueror.timecore.api.animation.BlendType;

public class TowerGuardianEntity extends Monster implements AnimatedObject<TowerGuardianEntity> {

    private static final String LAYER_WALKING = "walking";

    private final AnimationSystem<TowerGuardianEntity> animationSystem;

    public TowerGuardianEntity(EntityType<? extends TowerGuardianEntity> type, Level world) {
        super(type, world);

        animationSystem = AnimationSystems.forEntity(this,
                EntityPredefinedAnimations.builder()
                        .walkingAnimation(new PredefinedAnimation(LAYER_WALKING, AnimTestEntityAnimations.towerGuardianWalk.starter()))
                        .build(),
                builder ->
                        builder.addLayer(LAYER_WALKING, BlendType.OVERWRITE, 1F)
        );
    }

    @Override
    public @NotNull AnimationSystem<TowerGuardianEntity> animationSystem() {
        return animationSystem;
    }

    @Override
    protected void registerGoals() {
//        goalSelector.addGoal(0, new WaterAvoidingRandomWalkingGoal(this, 1.0D));//mutex 1
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15D);
    }
}
