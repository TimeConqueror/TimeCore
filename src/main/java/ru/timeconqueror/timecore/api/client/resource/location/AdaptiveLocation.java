package ru.timeconqueror.timecore.api.client.resource.location;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class AdaptiveLocation {
    private final String namespace;
    private final String path;

    /**
     * @param path represents the path to the texture.
     *             May contain part given from {@link #getPrefix()} to avoid confusion.
     */
    public AdaptiveLocation(String modid, String path) {
        this.namespace = modid;
        this.path = cutPath(path);
    }

    public AdaptiveLocation(ResourceLocation location) {
        this(location.getNamespace(), location.getPath());
    }

    @NotNull
    abstract String getPrefix();

    public ResourceLocation fullLocation() {
        return new ResourceLocation(getNamespace(), getFullPath());
    }

    /**
     * Returns location in string format without prefix.
     */
    @Override
    public String toString() {
        return getNamespace() + ":" + getPath();
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPath() {
        return path;
    }

    public String getFullPath() {
        return getPrefix() + getPath();
    }

    /**
     * Cuts all available prefixes if present.
     *
     * @return path without any prefixes.
     */
    private String cutPath(String path) {
        String[] availablePrefs = getAvailablePrefs();
        for (String prefix : availablePrefs) {
            if (path.startsWith(prefix)) {
                path = path.substring(prefix.length());
                break;
            }
        }

        return path;
    }

    /**
     * Available prefixes, that can be cutout.
     */
    private String[] getAvailablePrefs() {
        String[] prefArr = getPrefix().split("/");
        int prefArrLength = prefArr.length;

        int realPrefLength = prefArrLength;
        if (prefArr[prefArrLength - 1].isEmpty()) {
            realPrefLength--;
        }

        String[] cutoutPrefs = new String[realPrefLength];
        for (int i = 0; i < realPrefLength; i++) {

            StringBuilder pref = new StringBuilder();
            for (int j = i; j < realPrefLength; j++) {
                pref.append(prefArr[j]).append('/');
            }

            cutoutPrefs[i] = pref.toString();
        }

        return cutoutPrefs;
    }
}
