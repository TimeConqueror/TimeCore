package ru.timeconqueror.timecore.client.render.model.loading;

import com.mojang.math.Vector3f;
import net.minecraft.world.phys.Vec2;
import ru.timeconqueror.timecore.client.render.model.TimeModelCube;

public class TimeCubeDefinition {
    private final Vector3f origin;
    /**
     * The cube dimensions (x, y, z).
     */
    private final Vector3f size;
    /**
     * The starting point in the texture foo.png (x -> horizontal, y -> vertical) for that cube.
     */
    private final Vec2 uv;
    /**
     * Scale factor /Expands the cube, without expanding the UV mapping - useful for making armor look worn, and not part of the entity.
     */
    private final float inflate;
    private final boolean mirrored;

    public TimeCubeDefinition(Vector3f origin, Vector3f size, Vec2 uv, float inflate, boolean mirrored) {
        this.origin = origin;
        this.size = size;
        this.uv = uv;
        this.inflate = inflate;
        this.mirrored = mirrored;
    }

    public TimeModelCube bake(TimePartDefinition ownerPart, int textureWidth, int textureHeight) {
        //The position of the cube, relative to the entity origin - located at the bottom front left point of the cube.
        Vector3f origin = new Vector3f(this.origin.x() - ownerPart.getPivot().x(), -(this.origin.y() + size.y() - ownerPart.getPivot().y()), this.origin.z() - ownerPart.getPivot().z());

        float inflate = this.inflate;
        if (size.x() == 0 || size.y() == 0 || size.z() == 0) {
            inflate = Math.max(0.008F, inflate);
        }

        return new TimeModelCube(origin, size, uv, inflate, mirrored, textureWidth, textureHeight);
    }
}