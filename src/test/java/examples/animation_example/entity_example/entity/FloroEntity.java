package examples.animation_example.entity_example.entity;

import examples.animation_example.entity_example.registry.EntityAnimations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import ru.timeconqueror.timecore.animation.AnimationData;
import ru.timeconqueror.timecore.animation.AnimationSystem;
import ru.timeconqueror.timecore.animation.action.BakedActionFactory;
import ru.timeconqueror.timecore.animation.component.LoopMode;
import ru.timeconqueror.timecore.animation.entityai.AnimatedRangedAttackGoal;
import ru.timeconqueror.timecore.animation.predefined.EntityPredefinedAnimations;
import ru.timeconqueror.timecore.animation.predefined.PredefinedAnimation;
import ru.timeconqueror.timecore.api.animation.*;
import ru.timeconqueror.timecore.api.animation.action.ActionDefinitions;
import ru.timeconqueror.timecore.api.animation.action.BakedAction;

import java.util.EnumSet;

/**
 * How vanilla tasks work:
 * If flags, from which task's mutex consists of, are disabled, then tasks with this mutex won't be run.
 * <p>
 * If task has higher priority (lower number), than system will check if current task with lower priority is interruptible.
 * If it is so, then it will be finished, and new task will take its place
 * <p>
 * If task has a lower priority (higher number), it's checked by system if it can work in parallel (if mutex isn't the same).
 */
public class FloroEntity extends Monster implements RangedAttackMob, AnimatedObject<FloroEntity> {
    private static final EntityDataAccessor<Boolean> HIDDEN = SynchedEntityData.defineId(FloroEntity.class, EntityDataSerializers.BOOLEAN);
    private static final String LAYER_SHOWING = "showing";
    private static final String LAYER_WALKING = "walking";
    private static final String LAYER_ATTACK = "attack";

    private static final Lazy<AnimationStarter> REVEALING_ACTION_STARTER = Lazy.of(() -> EntityAnimations.floroReveal.starter());

    private final AnimationSystem<FloroEntity> animationSystem;
    //server side only
    private boolean isHiding = false;

    public FloroEntity(EntityType<? extends FloroEntity> type, Level level) {
        super(type, level);

        EntityPredefinedAnimations predefined = EntityPredefinedAnimations.builder()
                .walkingAnimation(new PredefinedAnimation(LAYER_WALKING, EntityAnimations.floroWalk.starter().withSpeed(3F)))
                .idleAnimation(new PredefinedAnimation(LAYER_WALKING, EntityAnimations.floroIdle.starter()))
                .build();

        animationSystem = AnimationSystems.forEntity(this, predefined, builder -> {
                    builder.addLayer(LAYER_SHOWING, BlendType.OVERWRITE, 1);
                    builder.addLayer(LAYER_WALKING, BlendType.ADD, 1);
                    builder.addLayer(LAYER_ATTACK, BlendType.ADD, 1);
                }
        );

        if (!level.isClientSide) {
//            animationSystem.getAnimationManager().getLayer(LAYER_SHOWING)
//                    .addAnimationEventListener(new AnimationDebugEventListener(animationSystem.getClock(), false));
        }
    }

    private static BakedActionFactory<FloroEntity> actionActivate(int animationTime, boolean activate) {
        return BakedActionFactory.builder(FloroEntity.class)
                .actionFactory(() -> BakedAction.sequencedBuilder(FloroEntity.class)
                        .id(activate ? "activate" : "deactivate")
//                        .appendAction(ActionDefinitions.everyCycleAt(animationTime, (ctx, props) -> {
//                            ctx.getOwner().isHiding = activate;
//                        }))
                        .build())
                .build();
    }

//    .actionFactory(() -> BakedAction.builder()
//                        .id(activate ? "activate" : "deactivate")
////                        .appendAction(ActionDefinitions.everyCycleAt(animationTime, (ctx, props) -> {
////                            ctx.getOwner().isHiding = activate;
////                        }))
//                        .build())

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(HIDDEN, true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putBoolean("hidden", isHidden());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains("hidden")) {
            setHidden(compound.getBoolean("hidden"));
        }
    }

    @Override
    public void onAddedToWorld() {
        if (isEffectiveAi() && isHidden()) {
            startHiddenAnimation();
        }
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloroRevealingGoal()); //mutex 1
        goalSelector.addGoal(1, new FloroHidingGoal()); //mutex 1
        goalSelector.addGoal(2, new FloroHiddenGoal()); //mutex 1
        goalSelector.addGoal(3, new FloatGoal(this));//mutex 4
        goalSelector.addGoal(4, new AvoidEntityGoal<>(this, Wolf.class, 6.0F, 1.0D, 1.2D));//mutex 1

        var rangedAttackBundle = AnimationBundle.<FloroEntity, AnimatedRangedAttackGoal.ActionProps>builder()
                .starter(EntityAnimations.floroShoot.starter())
                .layerName(LAYER_ATTACK)
                .action("SHOOT", ActionDefinitions.everyCycleAtPercents(0.5F, AnimatedRangedAttackGoal::redirectToPerformRangedAttack))
                .build();

        goalSelector.addGoal(5, new AnimatedRangedAttackGoal<>(this, rangedAttackBundle, 1.0F, 16.0F));//mutex 3

        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));//mutex 1
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));//mutex 2
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));//mutex 3

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 5/*will target if rand.next(chance) == 0*/, true, false, null));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 25)
                .add(Attributes.FOLLOW_RANGE, 25)
                .add(Attributes.MOVEMENT_SPEED, 0.26);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void aiStep() {
        if (isEffectiveAi()) {
            if (isHidden()) {
                isHiding = false;
            }

            if (canMove()) {
                goalSelector.enableControlFlag(Goal.Flag.LOOK);
                goalSelector.enableControlFlag(Goal.Flag.JUMP);
            } else {
                goalSelector.disableControlFlag(Goal.Flag.LOOK);
                goalSelector.disableControlFlag(Goal.Flag.JUMP); //actually doesn't help in some situations as these tasks still can be activated, if executingTasks list in EntityAITasks is empty.
            }
        }

        super.aiStep();
    }

    @Override
    public void push(double x, double y, double z) {
        if (canMove()) {
            super.push(x, y, z);
        } else {
            super.push(0, 0, 0);
        }
    }

    @Override
    public void knockback(double strength, double ratioX, double ratioZ) {
        if (canMove()) {
            super.knockback(strength, ratioX, ratioZ);
        }
    }

    private boolean canMove() {
        AnimationData data = animationSystem.getAnimationManager().getLayer(LAYER_SHOWING).getCurrentTicker().getAnimationData();
        return !isHidden() && !REVEALING_ACTION_STARTER.get().getData().equals(data) && !isHiding;
    }

    private void startHiddenAnimation() {
        animationSystem.startAnimation(EntityAnimations.floroReveal.starter()
                        .reversed()
                        .startingFrom(0)
                        .withLoopMode(LoopMode.HOLD_ON_LAST_FRAME)
                        .withTransitionTime(0),
                LAYER_SHOWING);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        FloroMudEntity projectile = new FloroMudEntity(this.level(), this, 0.5F);
        double dX = target.getX() - this.getX();
        double dY = target.getY(0.3333333333333333D) - projectile.getY();
        double dZ = target.getZ() - this.getZ();
        double distortion = Mth.sqrt((float) (dX * dX + dZ * dZ));
        projectile.shoot(dX, dY + distortion * 0.20000000298023224D, dZ, 1.6F, (float) (7 - this.level().getDifficulty().getId()));

        this.level().addFreshEntity(projectile);
    }

    public boolean isHidden() {
        return getEntityData().get(HIDDEN);
    }

    public void setHidden(boolean value) {
        getEntityData().set(HIDDEN, value);
    }


    public @NotNull AnimationSystem<FloroEntity> animationSystem() {
        return animationSystem;
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return sizeIn.height * 0.78F;
    }

    private class FloroRevealingGoal extends Goal {
        public FloroRevealingGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !canMove() && getTarget() != null;
        }

        @Override
        public void start() {
            animationSystem().startAnimation(AnimationBundle.<FloroEntity, Void>builder()
                            .starter(REVEALING_ACTION_STARTER.get())
                            .layerName(LAYER_SHOWING)
                            .action("REVEAL", ActionDefinitions.onBoundaryEnd((ctx, props) -> ctx.getOwner().setHidden(false)))
                            .build(),
                    null);
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }
    }

    private class FloroHiddenGoal extends Goal {
        public FloroHiddenGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return isHidden();
        }
    }

    private class FloroHidingGoal extends Goal {
        public FloroHidingGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !isHidden() && getTarget() == null;
        }

        @Override
        public boolean canContinueToUse() {
            return !isHidden();
        }

        @Override
        public void start() {
            isHiding = true;

            animationSystem().startAnimation(AnimationBundle.<FloroEntity, Void>builder()
                            .starter(EntityAnimations.floroReveal.starter().reversed().withLoopMode(LoopMode.HOLD_ON_LAST_FRAME))
                            .layerName(LAYER_SHOWING)
                            .action("HIDE", ActionDefinitions.onBoundaryEnd((ctx, props) -> ctx.getOwner().setHidden(true)))
                            .build(),
                    null);
        }

        @Override
        public void tick() {
        }

        @Override
        public void stop() {
            isHiding = false;
        }
    }
}
