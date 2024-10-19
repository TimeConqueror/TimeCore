package ru.timeconqueror.timecore.animation.entityai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import ru.timeconqueror.timecore.animation.util.AnimationUtils;
import ru.timeconqueror.timecore.api.animation.AnimatedObject;
import ru.timeconqueror.timecore.api.animation.AnimationBundle;

import java.util.EnumSet;
import java.util.function.BiConsumer;

public class AnimatedRangedAttackGoal<T extends Mob & AnimatedObject<T>> extends Goal {
    public static final BiConsumer<RangedAttackMob, ActionData> STANDARD_RUNNER =
            (entity, actionData) -> entity.performRangedAttack(actionData.getAttackTarget(), actionData.getDistanceFactor());

    private final T entity;
    private final double entityMoveSpeed;
    private final float attackInterval;
    private final float attackRadius;
    private final float maxAttackDistance;
    private final AnimationBundle<T, ActionData> animationBundle;
    private LivingEntity attackTarget;
    private int rangedAttackTime = -1;
    private int seeTime;

    public AnimatedRangedAttackGoal(T attacker, AnimationBundle<T, ActionData> animationBundle, double moveSpeed, float maxAttackDistance) {
        this.entity = attacker;
        this.entityMoveSpeed = moveSpeed;
        this.attackRadius = maxAttackDistance;
        this.maxAttackDistance = maxAttackDistance * maxAttackDistance;
        this.animationBundle = animationBundle;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));

        this.attackInterval = AnimationUtils.millisToTicks(
                animationBundle.getStarter().getData().getElapsedLengthTillFirstBoundary()
        );
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    @Override
    public boolean canUse() {
        LivingEntity livingentity = this.entity.getTarget();
        if (livingentity != null && livingentity.isAlive()) {
            this.attackTarget = livingentity;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean canContinueToUse() {
        return this.canUse() || !this.entity.getNavigation().isDone();
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    @Override
    public void stop() {
        this.attackTarget = null;
        this.seeTime = 0;
        this.rangedAttackTime = -1;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    @Override
    public void tick() {
        float d0 = (float) this.entity.distanceToSqr(this.attackTarget.getX(), this.attackTarget.getY(), this.attackTarget.getZ());
        boolean flag = this.entity.getSensing().hasLineOfSight(this.attackTarget);
        if (flag) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }

        if (!(d0 > (double) this.maxAttackDistance) && this.seeTime >= 5) {
            this.entity.getNavigation().stop();
        } else {
            this.entity.getNavigation().moveTo(this.attackTarget, this.entityMoveSpeed);
        }

        this.entity.getLookControl().setLookAt(this.attackTarget, 30.0F, 30.0F);
        if (--this.rangedAttackTime == 0) {
            if (!flag) {
                return;
            }

            float f = Mth.sqrt(d0) / this.attackRadius;
            float distanceFactor = Mth.clamp(f, 0.1F, 1.0F);

            ActionData actionData = new ActionData(distanceFactor, attackTarget);
            entity.animationSystem().startAnimation(animationBundle, actionData);

            this.rangedAttackTime = Mth.ceil(this.attackInterval);
        } else if (this.rangedAttackTime < 0) {
            this.rangedAttackTime = Mth.ceil(this.attackInterval);
        }
    }

    public static class ActionData {
        private final float distanceFactor;
        private final LivingEntity attackTarget;

        public ActionData(float distanceFactor, LivingEntity attackTarget) {
            this.distanceFactor = distanceFactor;
            this.attackTarget = attackTarget;
        }

        /**
         * This is an attack target, that was captured, when action was only started.
         * So, attack target may be invulnerable, already killed, etc, at the moment of damage action calling time,
         * that's why you need to check it there.
         */
        public LivingEntity getAttackTarget() {
            return attackTarget;
        }

        public float getDistanceFactor() {
            return distanceFactor;
        }
    }
}
