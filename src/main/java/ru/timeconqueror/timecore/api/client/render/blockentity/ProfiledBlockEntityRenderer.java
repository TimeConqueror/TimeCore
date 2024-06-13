package ru.timeconqueror.timecore.api.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class ProfiledBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private final BlockEntityRenderer<? super T> delegate;

    public ProfiledBlockEntityRenderer(BlockEntityRendererProvider.Context ctx, Function<? super BlockEntityRendererProvider.Context, BlockEntityRenderer<? super T>> delegateProvider) {
        super();

        delegate = delegateProvider.apply(ctx);
    }

    @Override
    public void render(T tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();

        profiler.push(() -> ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(tileEntityIn.getType()).toString());

        delegate.render(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);

        profiler.pop();
    }

    @Override
    public boolean shouldRenderOffScreen(T te) {
        return delegate.shouldRenderOffScreen(te);
    }

    @Override
    public int getViewDistance() {
        return delegate.getViewDistance();
    }

    @Override
    public boolean shouldRender(T blockEntity_, Vec3 cameraPos_) {
        return delegate.shouldRender(blockEntity_, cameraPos_);
    }
}
