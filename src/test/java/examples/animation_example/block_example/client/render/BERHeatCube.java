package examples.animation_example.block_example.client.render;

import examples.animation_example.block_example.block.entity.BlockEntityHeatCube;
import examples.animation_example.block_example.registry.BlockEntityModels;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import ru.timeconqueror.timecore.TimeCore;
import ru.timeconqueror.timecore.animation.renderer.AnimatedBlockEntityRenderer;
import ru.timeconqueror.timecore.animation.renderer.ModelConfiguration;
import ru.timeconqueror.timecore.api.client.resource.location.TextureLocation;
import ru.timeconqueror.timecore.client.render.model.TimeModel;

public class BERHeatCube extends AnimatedBlockEntityRenderer<BlockEntityHeatCube> {
    public BERHeatCube(BlockEntityRendererProvider.Context context) {
        super(new TimeModel(ModelConfiguration.builder(BlockEntityModels.HEAT_CUBE).build()));
    }

    @Override
    protected ResourceLocation getTexture(BlockEntityHeatCube blockEntity) {
        return new TextureLocation(TimeCore.MODID, "blockentity/spark_smelter.png").fullLocation();
    }
}
