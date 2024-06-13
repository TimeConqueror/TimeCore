package ru.timeconqueror.timecore.api.client.resource.location;

import net.minecraft.resources.ResourceLocation;

/**
 * Represents the location of the model.
 *
 * @see BlockModelLocation - model location for block.
 * @see ItemModelLocation - model location for item.
 */
public abstract class ModelLocation extends AdaptiveLocation {
    public ModelLocation(String modid, String path) {
        super(modid, path);
    }

    public ModelLocation(ResourceLocation location) {
        super(location);
    }

    public ResourceLocation fullLocation() {
        return new ResourceLocation(getNamespace(), getFullPath() + ".json");
    }
}
