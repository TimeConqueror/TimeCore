package ru.timeconqueror.timecore.api.animation;

import ru.timeconqueror.timecore.animation.AnimationSystem;
import ru.timeconqueror.timecore.animation.renderer.AnimatedLivingEntityRenderer;

/**
 * An interface for entities to provide animation stuff.
 * You also need to use {@link AnimatedLivingEntityRenderer} for animations to work.
 * <br>
 * Example of implementing:
 * <blockquote><pre>
 *  public class EntityFloro extends Monster implements AnimatedObject<EntityFloro> {
 *     private final ActionController<EntityFloro> actionController;
 *
 *     public EntityFloro(EntityType<? extends MonsterEntity> type, World worldIn) {
 *         super(type, worldIn);
 *
 *         EntityPredefinedAnimations predefined = EntityPredefinedAnimations.builder()
 *                 .walkingAnimation(new PredefinedAnimation(LAYER_WALKING, EntityAnimations.floroWalk.starter().withSpeed(3F)))
 *                 .idleAnimation(new PredefinedAnimation(LAYER_WALKING, EntityAnimations.floroIdle.starter()))
 *                 .build();
 *
 *         animationSystem = AnimationSystems.forEntity(this, predefined, builder -> {
 *                     builder.addLayer(LAYER_SHOWING, BlendType.OVERWRITE, 1);
 *                     builder.addLayer(LAYER_WALKING, BlendType.ADD, 1);
 *                     builder.addLayer(LAYER_ATTACK, BlendType.ADD, 1);
 *                 }
 *         );
 *     }
 *
 *     public @NotNull AnimationSystem<FloroEntity> getSystem() {
 *         return animationSystem;
 *     }
 * }
 * </pre></blockquote>
 *
 * @see AnimatedLivingEntityRenderer
 */
public interface AnimatedObject<T extends AnimatedObject<T>> {
    /**
     * The entry point for accessing animation stuff.
     */
    AnimationSystem<T> animationSystem();

    default void populateMolangObjects(MolangObjectFiller molangObjectFiller) {

    }
}
