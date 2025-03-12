package net.moss.resonance.block.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.moss.resonance.Resonance;
import net.moss.resonance.block.TintedBottleBlock;
import net.moss.resonance.block.entity.TintedBottleBlockEntity;

@Environment(value=EnvType.CLIENT)
public class TintedBottleBlockEntityRenderer implements BlockEntityRenderer<TintedBottleBlockEntity> {
    private static final Identifier TEXTURE = Identifier.of(Resonance.MOD_ID, "textures/block/vent_fluid.png");
	private final ModelPart fluid_low;
	private final ModelPart fluid_high;

    public TintedBottleBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData fluid_low = modelPartData.addChild("fluid_low", ModelPartBuilder.create().uv(0, 0).cuboid(-2.5f, 0.5f, -2.5f, 5, 6, 5, new Dilation(0.0F)), ModelTransform.of(8f, 0, 8f, 0, 0, 0));
        ModelPartData fluid_high = modelPartData.addChild("fluid_high", ModelPartBuilder.create().uv(0, 0).cuboid(-2.5f, 1.5f, -2.5f, 5, 6, 5, new Dilation(0.0F)), ModelTransform.of(8f, 0, 8f, 0, 0, 0));
		this.fluid_low = fluid_low.createPart(32, 32);
		this.fluid_high = fluid_high.createPart(32, 32);
    }

    @Override
    public void render(TintedBottleBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {  
        BlockState blockState = entity.getCachedState();
        if (!blockState.isOf(Resonance.VENT_FLUID_BOTTLE_BLOCK)) { return; }
        int color = entity.getColor();
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));
        if (blockState.get(TintedBottleBlock.HANGING)) {
            this.fluid_high.render(matrices, consumer, light, overlay, color);
        } else {
            this.fluid_low.render(matrices, consumer, light, overlay, color);
        }
    }
}