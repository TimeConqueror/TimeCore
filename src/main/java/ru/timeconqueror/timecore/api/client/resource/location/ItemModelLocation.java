package ru.timeconqueror.timecore.api.client.resource.location;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ItemModelLocation extends ModelLocation {
    /**
     * @param path represents the path to the model.
     *             May contain "models/item", "item/" part to avoid confusion.
     */
    public ItemModelLocation(String modid, String path) {
        super(modid, path);
    }

    public ItemModelLocation(ResourceLocation location) {
        super(location);
    }

    @Override
    @NotNull
    String getPrefix() {
        return "models/item/";
    }

    @Override
    public String toString() {
        return getNamespace() + ":item/" + getPath();
    }
}
