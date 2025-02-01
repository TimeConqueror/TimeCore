package ru.timeconqueror.timecore.animation.loading;

import com.google.gson.*;
import gg.moonflower.molangcompiler.api.MolangCompiler;
import gg.moonflower.molangcompiler.api.MolangExpression;
import lombok.SneakyThrows;
import org.joml.Vector3f;
import ru.timeconqueror.timecore.TimeCore;
import ru.timeconqueror.timecore.animation.loading.VectorDefinition.ConstantVectorDefinition;
import ru.timeconqueror.timecore.animation.loading.VectorDefinition.DynamicVectorDefinition;

import java.lang.reflect.Type;

//FIXME check parse("0.0") is a ConstantNode?
public class VectorDefinitionDeserializer implements JsonDeserializer<VectorDefinition> {
    private final MolangCompiler compiler = TimeCore.INSTANCE.getMolangCompiler();

    @Override
    public VectorDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonArray() && !json.isJsonPrimitive()) {
            throw new JsonSyntaxException("Expected to read an array or a primitive");
        }

        if (json.isJsonPrimitive()) {
            return deserializeFromSingle(json);
        }

        JsonArray array = json.getAsJsonArray();
        if (array.size() != 3 && array.size() != 1) {
            throw new JsonSyntaxException("Expected 1 or 3 elements in vec3f array, found: " + array.size());
        }

        if (array.size() == 1) {
            return deserializeFromSingle(array.get(0));
        } else {
            return deserialize(array.get(0), array.get(1), array.get(2));
        }
    }

    @SneakyThrows
    private VectorDefinition deserializeFromSingle(JsonElement primitive) {
        try {
            float f = primitive.getAsFloat();
            return new ConstantVectorDefinition(new Vector3f(f, f, f));
        } catch (NumberFormatException nfe) {
            MolangExpression exp = compiler.compile(primitive.getAsString().trim());
            return new DynamicVectorDefinition(exp);
        }
    }

    @SneakyThrows
    private VectorDefinition deserialize(JsonElement x, JsonElement y, JsonElement z) {
        try {
            return new ConstantVectorDefinition(new Vector3f(x.getAsFloat(), y.getAsFloat(), z.getAsFloat()));
        } catch (NumberFormatException nfe) {
            return new DynamicVectorDefinition(parse(x), parse(y), parse(z));
        }
    }

    @SneakyThrows
    private MolangExpression parse(JsonElement primitive) {
        try {
            return MolangExpression.of(primitive.getAsFloat());
        } catch (NumberFormatException ex) {
            return compiler.compile(primitive.getAsString().trim());
        }
    }
}
