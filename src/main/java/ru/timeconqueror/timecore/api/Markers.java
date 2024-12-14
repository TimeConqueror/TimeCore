package ru.timeconqueror.timecore.api;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.HashSet;
import java.util.Set;

public class Markers {
    private static final Set<Marker> ALL_MARKERS = new HashSet<>();

    public static final Marker RESOURCES = register("RESOURCES");
    public static final Marker ANIMATIONS = register("ANIMATIONS");
    public static final Marker ACTIONS = register("ACTIONS");

    private static Marker register(String name) {
        Marker marker = MarkerManager.getMarker(name);
        ALL_MARKERS.add(marker);
        return marker;
    }

    public static Marker[] all() {
        return ALL_MARKERS.toArray(new Marker[0]);
    }
}
