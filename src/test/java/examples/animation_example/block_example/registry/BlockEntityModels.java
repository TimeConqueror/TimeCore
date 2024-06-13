package examples.animation_example.block_example.registry;

import ru.timeconqueror.timecore.TimeCore;
import ru.timeconqueror.timecore.api.registry.TimeModelRegister;
import ru.timeconqueror.timecore.api.registry.util.AutoRegistrable;
import ru.timeconqueror.timecore.client.render.model.InFileLocation;

public class BlockEntityModels {
    @AutoRegistrable
    private static final TimeModelRegister REGISTER = new TimeModelRegister(TimeCore.MODID);
    public static InFileLocation HEAT_CUBE = REGISTER.register("models/blockentity/heat_cube.json");
}
