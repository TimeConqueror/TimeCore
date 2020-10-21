package ru.timeconqueror.timecore.client.render.model;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3f;
import org.jetbrains.annotations.NotNull;
import ru.timeconqueror.timecore.api.util.CollectionUtils;
import ru.timeconqueror.timecore.client.render.JsonParsingException;
import ru.timeconqueror.timecore.util.JsonUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class JsonModelParser {
    private static final String[] ACCEPTABLE_FORMAT_VERSIONS = new String[]{"1.12.0"};

    public List<TimeModelFactory> parseJsonModel(@NotNull ResourceLocation fileLocation) throws JsonParsingException {
        try (final IResource resource = Minecraft.getInstance().getResourceManager().getResource(fileLocation)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));

            JsonObject json = JSONUtils.parse(reader, true/*isLenient*/);
            return parseJsonModel(json);

        } catch (Throwable e) {
            throw new JsonParsingException(e);
        }
    }

    private List<TimeModelFactory> parseJsonModel(JsonObject object) throws JsonParsingException {
        List<TimeModelFactory> modelFactories = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (entry.getKey().equals("format_version")) {
                String formatVersion = entry.getValue().getAsString();
                checkFormatVersion(formatVersion);
            } else {
                TimeModelFactory modelFactory = parseSubModel(entry.getKey(), entry.getValue().getAsJsonArray());
                modelFactories.add(modelFactory);
            }
        }

        return modelFactories;
    }

    private TimeModelFactory parseSubModel(String name, JsonArray subModelArr) throws JsonParsingException {
        JsonObject subModel = subModelArr.get(0).getAsJsonObject();
        JsonArray bones = subModel.get("bones").getAsJsonArray();

        JsonObject description = subModel.get("description").getAsJsonObject();
        int textureWidth = JsonUtils.getInt("texture_width", description);
        int textureHeight = JsonUtils.getInt("texture_height", description);

        HashMap<String, RawModelBone> pieces = new HashMap<>();
        for (JsonElement bone : bones) {
            RawModelBone piece = parseBone(bone);
            pieces.put(piece.name, piece);
        }

        List<RawModelBone> rootPieces = new ArrayList<>();
        for (RawModelBone value : pieces.values()) {
            if (value.parentName != null) {
                RawModelBone parent = pieces.get(value.parentName);
                if (parent != null) {
                    if (parent.children == null) parent.children = new ArrayList<>();

                    parent.children.add(value);
                } else {
                    throw new JsonParsingException("Can't find parent node " + value.parentName + " for node " + value.name);
                }
            } else {
                rootPieces.add(value);
            }
        }

        return create(name, textureWidth, textureHeight, rootPieces);
    }

    private TimeModelFactory create(String name, int textureWidth, int textureHeight, List<RawModelBone> rootPieces) {
        return renderTypeProvider -> {
            TimeModel model = new TimeModel(renderTypeProvider, name, textureWidth, textureHeight);
            model.setPieces(rootPieces.stream().map(rawModelBone -> rawModelBone.bake(model, null)).collect(Collectors.toList()));

            return model;
        };
    }

    private RawModelBone parseBone(JsonElement bone) throws JsonParsingException {
        Vector3f pivot = JsonUtils.getVec3f("pivot", bone);
        Vector3f rotationAngles = JsonUtils.getVec3f("rotation", bone, new Vector3f(0, 0, 0));
        boolean mirror = JsonUtils.getBoolean("mirror", bone, false);
        boolean neverRender = JsonUtils.getBoolean("neverrender", bone, false);
        float inflate = JsonUtils.getFloat("inflate", bone, 0F);
        String name = JsonUtils.getString("name", bone);
        String parentName = JsonUtils.getString("parent", bone, null);

        List<RawModelBone> extraBones = new ArrayList<>();

        List<RawModelCube> cubes = new ArrayList<>();
        if (bone.getAsJsonObject().has("cubes")) {
            for (JsonElement cube : bone.getAsJsonObject().get("cubes").getAsJsonArray()) {
                Vector3f origin = JsonUtils.getVec3f("origin", cube);
                Vector3f size = JsonUtils.getVec3f("size", cube);
                Vector2f uv = JsonUtils.getVec2f("uv", cube);

                if (cube.getAsJsonObject().has("rotation") || cube.getAsJsonObject().has("inflate") || cube.getAsJsonObject().has("mirror")) {
                    Vector3f rotation = JsonUtils.getVec3f("rotation", cube, new Vector3f(0, 0, 0));
                    Vector3f innerPivot = JsonUtils.getVec3f("pivot", cube, new Vector3f(0, 0, 0));
                    boolean innerMirror = JsonUtils.getBoolean("mirror", cube, false);

                    float cubeInflate = JsonUtils.getFloat("inflate", cube, 0F);
                    extraBones.add(new RawModelBone(Lists.newArrayList(new RawModelCube(origin, size, uv)), innerPivot, rotation, innerMirror, false, cubeInflate, "cube_wrapper_" + extraBones.size(), name));
                } else {
                    cubes.add(new RawModelCube(origin, size, uv));
                }
            }
        }

        RawModelBone rawModelBone = new RawModelBone(cubes, pivot, rotationAngles, mirror, neverRender, inflate, name, parentName);
        rawModelBone.children = extraBones;
        return rawModelBone;
    }

    private void checkFormatVersion(String version) throws JsonParsingException {
        if (!CollectionUtils.contains(ACCEPTABLE_FORMAT_VERSIONS, version)) {
            throw new JsonParsingException("The format version " + version + " is not supported. Supported versions: " + Arrays.toString(ACCEPTABLE_FORMAT_VERSIONS));
        }
    }

    public static class RawModelBone {
        private final List<RawModelCube> cubes;
        private final Vector3f pivot;
        private final Vector3f rotationAngles;
        private final boolean mirror;
        private final boolean neverRender;
        private final float inflate;
        private final String name;
        private final String parentName;

        private List<RawModelBone> children;

        private RawModelBone(List<RawModelCube> cubes, Vector3f pivot, Vector3f rotationAngles, boolean mirror, boolean neverRender, float inflate, String name, String parentName) {
            this.cubes = cubes;
            this.pivot = pivot;
            this.rotationAngles = rotationAngles;
            this.mirror = mirror;
            this.neverRender = neverRender;
            this.inflate = inflate;
            this.name = name;
            this.parentName = parentName;
        }

        private TimeModelRenderer bake(TimeModel model, RawModelBone parent) {
            List<TimeModelBox> boxesOut = new ArrayList<>(cubes.size());
            for (RawModelCube cube : cubes) {
                boxesOut.add(cube.bake(model, this));
            }

            Vector3f rotationAnglesRadians = new Vector3f(rotationAngles.x() * (float) Math.PI / 180,
                    rotationAngles.y() * (float) Math.PI / 180,
                    rotationAngles.z() * (float) Math.PI / 180);

            TimeModelRenderer renderer = new TimeModelRenderer(model, rotationAnglesRadians, name, boxesOut, neverRender);
            if (parent != null) {
                renderer.setPos(pivot.x() - parent.pivot.x(), -(pivot.y() - parent.pivot.y()), pivot.z() - parent.pivot.z());
            } else renderer.setPos(pivot.x(), -pivot.y(), pivot.z());

            if (children != null) {
                for (RawModelBone child : children) {
                    renderer.children.add(child.bake(model, this));
                }
            }

            return renderer;
        }
    }

    public static class RawModelCube {
        private final Vector3f origin;
        private final Vector3f size;
        private final Vector2f uv;

        private RawModelCube(Vector3f origin, Vector3f size, Vector2f uv) {
            this.origin = origin;
            this.size = size;
            this.uv = uv;
        }

        private TimeModelBox bake(TimeModel model, RawModelBone bone) {
            origin.set(origin.x() - bone.pivot.x(), -(origin.y() + size.y() - bone.pivot.y()), origin.z() - bone.pivot.z());
            return new TimeModelBox(origin, size, uv, bone.inflate, bone.mirror, model.texWidth, model.texHeight);
        }
    }
}
