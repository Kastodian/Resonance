package net.moss.resonance.block.renderer;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.block.RainChimeBlock;
import net.moss.resonance.block.entity.RainChimeBlockEntity;

public class RainChimeBlockRenderer implements BlockEntityRenderer<RainChimeBlockEntity> {
	private static final Identifier TEXTURE = Identifier.of(Resonance.MOD_ID, "textures/block/rain_chime.png");
	private final ModelPart base;

	public RainChimeBlockRenderer(BlockEntityRendererFactory.Context context) {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData base = modelPartData.addChild("base", ModelPartBuilder.create().uv(0, 0).cuboid(-2, 0, -2, 4, 2, 4, new Dilation(0.0F)), ModelTransform.pivot(8, 14, 8));
		ModelPartData String1 = base.addChild("string1", ModelPartBuilder.create().uv(13, 6).cuboid(0, -1, -0.5F, 0, 1, 1, new Dilation(0.0F)), ModelTransform.of(-1.5F, 0, -1.5F, 0, -0.7854F, 0));
		String1.addChild("chime1", ModelPartBuilder.create().uv(0, 6).cuboid(-0.5F, -5, -0.5F, 1, 5, 1, new Dilation(0.0F)), ModelTransform.pivot(0, -1, 0));
		ModelPartData String2 = base.addChild("string2", ModelPartBuilder.create().uv(13, 6).cuboid(0, -2, -0.5F, 0, 2, 1, new Dilation(0.0F)), ModelTransform.of(-1.5F, 0, 1.5F, 0, -0.7854F, 0));
		String2.addChild("chime2", ModelPartBuilder.create().uv(4, 6).cuboid(-1, -7, -1, 2, 7, 2, new Dilation(0.0F)), ModelTransform.pivot(0, -2, 0));
		ModelPartData String3 = base.addChild("string3", ModelPartBuilder.create().uv(13, 6).cuboid(0, -3, -0.5F, 0, 3, 1, new Dilation(0.0F)), ModelTransform.of(1.5F, 0, 1.5F, 0, -0.7854F, 0));
		String3.addChild("chime3", ModelPartBuilder.create().uv(0, 6).cuboid(-0.5F, -5, -0.5F, 1, 5, 1, new Dilation(0.0F)), ModelTransform.pivot(0, -3, 0));
		ModelPartData String4 = base.addChild("string4", ModelPartBuilder.create().uv(13, 6).cuboid(0, -3, -0.5F, 0, 3, 1, new Dilation(0.0F)), ModelTransform.of(1.5F, 0, -1.5F, 0, -0.7854F, 0));
		String4.addChild("chime4", ModelPartBuilder.create().uv(4, 6).cuboid(-1, -7, -1, 2, 7, 2, new Dilation(0.0F)), ModelTransform.pivot(0, -3, 0));
		this.base = base.createPart(16, 16);
	}

	@Override
	public void render(RainChimeBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		World world = entity.getWorld();
		if (world == null) { return; }
		float correctedTicks = (float) ((world.getTime() % 314.15) + tickDelta);
		float rainCorrection = world.isThundering() ? 0.16f : world.isRaining() ? 0.06f : 0.0f;
		float ringingCorrection = entity.getCachedState().get(RainChimeBlock.AGITATED) ? 0.2f : 0.0f;
		rainCorrection += ringingCorrection;
		this.base.getChild("string2").roll = MathHelper.sin(correctedTicks * (0.02f + rainCorrection)) * (0.08f + rainCorrection);
		this.base.getChild("string1").roll = MathHelper.sin(correctedTicks * (0.04f + rainCorrection)) * (0.06f + rainCorrection);
		this.base.getChild("string4").roll = MathHelper.sin(correctedTicks * (0.06f + rainCorrection)) * (0.04f + rainCorrection);
		this.base.getChild("string3").roll = MathHelper.sin(correctedTicks * (0.08f + rainCorrection)) * (0.02f + rainCorrection);
		this.base.getChild("string2").getChild("chime2").roll = MathHelper.sin(correctedTicks * 0.02f + rainCorrection) * (0.08f + rainCorrection);
		this.base.getChild("string1").getChild("chime1").roll = MathHelper.sin(correctedTicks * 0.04f + rainCorrection) * (0.06f + rainCorrection);
		this.base.getChild("string4").getChild("chime4").roll = MathHelper.sin(correctedTicks * 0.06f + rainCorrection) * (0.04f + rainCorrection);
		this.base.getChild("string3").getChild("chime3").roll = MathHelper.sin(correctedTicks * 0.08f + rainCorrection) * (0.02f + rainCorrection);
		this.base.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE)), light, overlay);
	}
}