package ru.timeconqueror.timecore.client.render.model;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import org.jetbrains.annotations.Nullable;
import ru.timeconqueror.timecore.animation.renderer.ModelConfiguration;
import ru.timeconqueror.timecore.api.client.render.model.ITimeModel;
import ru.timeconqueror.timecore.api.util.client.DrawHelper;
import ru.timeconqueror.timecore.internal.client.handlers.ClientLoadingHandler;

import java.util.Map;


public class TimeModel extends Model implements ITimeModel {
    private final InFileLocation location;
    private final ReloadableContainer container = new ReloadableContainer();

    public TimeModel(ModelConfiguration modelConfig) {
        super(modelConfig.renderTypeProvider());

        this.location = modelConfig.location();
    }

    @Override
    public InFileLocation getLocation() {
        return location;
    }

    @Override
    public TimeModelPart getPart(String partName) {
        TimeModelPart part = tryGetPart(partName);
        if (part == null)
            throw new IllegalArgumentException(String.format("Part '%s' was not found in the model '%s'", partName, location));

        return part;
    }

    @Nullable
    public TimeModelPart tryGetPart(String partName) {
        return container.getPartMap().get(partName);
    }

    public TimeModelPart getRoot() {
        return container.getRoot();
    }

    /**
     * Should be called before animation applying & render.
     */
    public void reset() {
        getRoot().reset();

        for (TimeModelPart part : container.getPartMap().values()) {
            part.reset();
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int rgba) {
        renderToBuffer(poseStack, buffer, packedLight, packedOverlay,
                DrawHelper.getRed(rgba) / 255F,
                DrawHelper.getGreen(rgba) / 255F,
                DrawHelper.getBlue(rgba) / 255F,
                DrawHelper.getAlpha(rgba) / 255F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        getRoot().render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private class ReloadableContainer extends TimeModelSet.ReloadListener {
        private TimeModelPart root;
        private Map<String, TimeModelPart> partMap;

        public TimeModelPart getRoot() {
            check(root);
            return root;
        }

        public Map<String, TimeModelPart> getPartMap() {
            check(partMap);
            return partMap;
        }

        private void check(Object object) {
            if (object == null) {
                throw new IllegalStateException("Reloadable parts were not loaded yet");
            }
        }

        @Override
        void reload() {
            root = ClientLoadingHandler.MODEL_SET.bakeRoot(location);
            buildPartMap();
        }

        private void buildPartMap() {
            ImmutableMap.Builder<String, TimeModelPart> builder = ImmutableMap.builder();

            // root itself is not placed to part map, because it is made by TimeCore itself and has i$root name
            for (Map.Entry<String, TimeModelPart> e : root.getChildren().entrySet()) {
                addPartToMap(builder, e.getKey(), e.getValue());
            }

            this.partMap = builder.build();
        }

        private void addPartToMap(ImmutableMap.Builder<String, TimeModelPart> builder, String name, TimeModelPart part) {
            builder.put(name, part);

            Map<String, TimeModelPart> children = part.getChildren();
            for (Map.Entry<String, TimeModelPart> e : children.entrySet()) {
                addPartToMap(builder, e.getKey(), e.getValue());
            }
        }
    }
}
