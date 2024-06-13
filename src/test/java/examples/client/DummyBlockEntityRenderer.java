package examples.client;

import com.mojang.blaze3d.vertex.PoseStack;
import examples.block.blockentity.DummyBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DummyBlockEntityRenderer implements BlockEntityRenderer<DummyBlockEntity> {

    public DummyBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(DummyBlockEntity tileEntityIn, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

    }
}
