package ru.timeconqueror.timecore.animation.loading;

import com.google.gson.*;
import lombok.AllArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import ru.timeconqueror.timecore.animation.component.AnimationBone;
import ru.timeconqueror.timecore.animation.component.BasicAnimation;
import ru.timeconqueror.timecore.animation.component.LoopMode;
import ru.timeconqueror.timecore.api.util.Empty;
import ru.timeconqueror.timecore.api.util.EnvironmentUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class AnimationDefinition {
    private final LoopMode loopMode;
    private final int animationLength;
    private final List<AnimationBone> options;

    public BasicAnimation bake(ResourceLocation id, String name) {
        Map<String, AnimationBone> bones = options.stream().collect(Collectors.toMap(AnimationBone::getName, animationBone -> animationBone));
        return new BasicAnimation(loopMode, id, name, animationLength, Collections.unmodifiableMap(bones));
    }

    public static class Deserializer implements JsonDeserializer<AnimationDefinition> {

        @Override
        public AnimationDefinition deserialize(JsonElement jsonIn, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject json = jsonIn.getAsJsonObject();

            LoopMode loopMode = LoopMode.DO_NOT_LOOP;
            if (json.has("loop")) {
                loopMode = context.deserialize(json.get("loop"), LoopMode.class);
            }

            int animationLength = (int) (GsonHelper.getAsFloat(json, "animation_length", 0) * 1000);

            List<AnimationBone> animationBones = new ArrayList<>();

            if (EnvironmentUtils.isOnPhysicalClient() && json.has("bones")) {
                for (Map.Entry<String, JsonElement> boneEntryJson : GsonHelper.getAsJsonObject(json, "bones").entrySet()) {
                    String boneName = boneEntryJson.getKey();
                    JsonObject boneJson = GsonHelper.convertToJsonObject(boneEntryJson.getValue(), boneName);

                    AnimationBoneDefinition boneDefinition = context.deserialize(boneJson, AnimationBoneDefinition.class);
                    AnimationBone option = boneDefinition.bake(boneName);
                    animationBones.add(option);
                }
            }

            return new AnimationDefinition(loopMode, animationLength, !animationBones.isEmpty() ? animationBones : Empty.list());
        }
    }
}
