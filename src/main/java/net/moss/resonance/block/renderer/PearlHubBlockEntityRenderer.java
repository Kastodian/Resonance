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
import net.moss.resonance.block.PearlHub;
import net.moss.resonance.block.entity.PearlHubBlockEntity;

@Environment(value=EnvType.CLIENT)
public class PearlHubBlockEntityRenderer implements BlockEntityRenderer<PearlHubBlockEntity> {
    private static final Identifier TEXTURE = Identifier.of(Resonance.MOD_ID, "textures/block/pearl.png");
	private final ModelPart pearl;
    private final float height = 7.01f;

    public PearlHubBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData pearl = modelPartData.addChild("pearl", ModelPartBuilder.create().uv(0, 0).cuboid(-3, height, -3, 6, 6, 6, new Dilation(0.0F)), ModelTransform.of(8, 0, 8, 0, 0.785F, 0));
        pearl.addChild("pearlCore", ModelPartBuilder.create().uv(0, 12).cuboid(-2.5f, height + 0.5f, -2.5f, 5, 5, 5, new Dilation(0.0F)), ModelTransform.NONE);
		this.pearl = pearl.createPart(32, 32);
    }

    @Override
    public void render(PearlHubBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState blockState = entity.getCachedState();
        if (!blockState.get(PearlHub.HAS_PEARL)) { return; }
        
		Identifier texture = entity.getTexture();
        if (texture == null) { texture = TEXTURE; }
		else if (!(texture.toString().length() > 0)) { texture = TEXTURE; }
        if (blockState.get(PearlHub.EXPENDED)) { light /= 2; }
		VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(texture));
		this.pearl.render(matrices, consumer, light, overlay);
    }
}