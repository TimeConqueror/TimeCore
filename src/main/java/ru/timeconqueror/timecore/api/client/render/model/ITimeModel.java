package ru.timeconqueror.timecore.api.client.render.model;

import org.jetbrains.annotations.Nullable;
import ru.timeconqueror.timecore.client.render.model.TimeModelLocation;
import ru.timeconqueror.timecore.client.render.model.TimeModelPart;

public interface ITimeModel {
    TimeModelLocation getLocation();

    ITimeModel setScaleMultiplier(float scaleMultiplier);

    //FIXME javadoc
    @Nullable TimeModelPart tryGetPart(String partName);

    //FIXME javadoc
    TimeModelPart getPart(String partName);

    /**
     * Should be called before animation applying & render.
     */
    void reset();
}
