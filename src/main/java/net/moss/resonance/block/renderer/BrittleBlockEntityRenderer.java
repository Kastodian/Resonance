package net.moss.resonance.block.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.World;
import net.moss.resonance.block.entity.BrittleBlockEntity;

@Environment(value=EnvType.CLIENT)
public class BrittleBlockEntityRenderer implements BlockEntityRenderer<BrittleBlockEntity> {
    private final BlockRenderManager blockRenderManager;
    
    public BrittleBlockEntityRenderer(BlockEntityRendererFactory.Context context) { this.blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager(); }

    @Override
    public void render(BrittleBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        World world = entity.getWorld();
        BlockState blockState = entity.getStoredState();
        // matrices.translate(0.001f, 0.001f, 0.001f);
        // matrices.scale(0.998f, 0.998f, 0.998f);
        this.blockRenderManager.renderBlock(blockState, entity.getPos(), world, matrices, vertexConsumers.getBuffer(RenderLayers.getBlockLayer(blockState)), false, world.getRandom());
    }
}