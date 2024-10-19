package ru.timeconqueror.timecore.api.animation;

import org.jetbrains.annotations.NotNull;
import ru.timeconqueror.timecore.api.client.render.model.ITimeModel;
import ru.timeconqueror.timecore.molang.SharedMolangObject;

import java.util.Set;

public interface AnimationManager {

	/**
	 * Returns true, if this animation manager contains the layer with provided location,
	 * otherwise returns false.
	 */
	boolean containsLayer(String name);

	/**
     * Returns layer object by its location.
     *
     * @throws RuntimeException if layer is not found,
     *                          so you should check existing of the layer in {@link #containsLayer(String)} firstly.
     */
    @NotNull
    Layer getLayer(String name);

	/**
	 * Returns all layer names.
	 * Can be used for iterating throw the layers by their location in {@link #getLayer(String)}
	 */
	Set<String> getLayerNames();

	/**
	 * On client: called on every frame for model from the renderer of entity, which contains this manager.
	 * On server: called on every tick and with null model as a param,
	 * since no operations with model shouldn't be done on server (because no model exists on server side).
	 *
	 * @param model        model to perform calculations on it.
	 *                     <p>
	 *                     on client: model of the bound entity;<p>
	 *                     on server: null
	 * @param partialTick
	 */
	void applyAnimations(ITimeModel model, float partialTick);

	void tick();

	boolean startAnimationScript(AnimationScript animationScript, String layerName);

    void stopAnimation(String layerName, int transitionTime);

	SharedMolangObject getSharedMolangObjects();
}
