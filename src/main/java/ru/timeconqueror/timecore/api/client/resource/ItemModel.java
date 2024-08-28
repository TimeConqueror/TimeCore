package ru.timeconqueror.timecore.api.client.resource;

import ru.timeconqueror.timecore.api.client.resource.location.ModelLocation;
import ru.timeconqueror.timecore.api.client.resource.location.TextureLocation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ItemModel extends JSONTimeResource {
    /**
     * Represents the location of the model parent.
     */
    private final ModelLocation parent;
    private final Map<String, TextureLocation> textureMap = new HashMap<>(1);
    private int layerCounter;

    public ItemModel(StandardItemModelParents parent) {
        this(parent.getModelLocation());
    }

    public ItemModel(ModelLocation parent) {
        this.parent = parent;
    }

    @Override
    public String toJson() {
        String texturesValue = null;
        if (!textureMap.isEmpty()) {
            texturesValue = listOf(() -> {
                return textureMap.entrySet()
                        .stream()
                        .map(e -> property(e.getKey(), e.getValue().toString()))
                        .toArray(String[]::new);
            });
        }

        if (texturesValue == null) {
            return object(null, listOf(
                    property("parent", parent.toString())
            ));
        } else {
            return object(null, listOf(
                    property("parent", parent.toString()),
                    object("textures", texturesValue))
            );
        }
    }

    public ItemModel addTexture(String key, TextureLocation textureLocation) {
        textureMap.put(key, textureLocation);

        return this;
    }

    /**
     * Adds texture layer to the model.
     * <p>
     * Commonly you will need to provide only one texture to the model,
     * but sometimes you will need to set model to use combination of several textures.
     * Vanilla uses it in, for example, spawn egg model where the layers are represented by base texture and overlay (spots).
     */
    public ItemModel addTextureLayer(TextureLocation textureLocation) {
        addTexture("layer" + layerCounter, textureLocation);
        layerCounter++;

        return this;
    }

    /**
     * Adds texture layer to the model.
     * <p>
     * Commonly you will need to provide only one texture to the model,
     * but sometimes you will need to set model to use combination of several textures.
     * Vanilla uses it in, for example, spawn egg model where the layers are represented by base texture and overlay (spots).
     */
    public ItemModel addTextureLayers(TextureLocation... textureLocations) {
        Arrays.stream(textureLocations)
                .forEach(this::addTextureLayer);

        return this;
    }
}
