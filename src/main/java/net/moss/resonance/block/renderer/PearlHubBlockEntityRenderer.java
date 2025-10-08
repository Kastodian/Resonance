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
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.moss.resonance.Resonance;
import net.moss.resonance.block.PearlHubBlock;
import net.moss.resonance.block.entity.PearlHubBlockEntity;
import net.moss.resonance.util.abstracts.BlockEntityWithNametagRenderer;

@Environment(value=EnvType.CLIENT)
public class PearlHubBlockEntityRenderer extends BlockEntityWithNametagRenderer<PearlHubBlockEntity> {
	private final ModelPart pearl;
	private final float height = 7.01f;

	public PearlHubBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
		super(context);
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData pearl = modelPartData.addChild("pearl", ModelPartBuilder.create().uv(0, 0).cuboid(-3, height, -3, 6, 6, 6, new Dilation(0.0F)), ModelTransform.of(8, 0, 8, 0, 0, 0));
		pearl.addChild("pearlCore", ModelPartBuilder.create().uv(0, 12).cuboid(-2.5f, height + 0.25f, -2.5f, 5, 5, 5, new Dilation(0.0F)), ModelTransform.NONE);
		this.pearl = pearl.createPart(32, 32);
	}

	@Override
	public void render(PearlHubBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		BlockState state = entity.getCachedState();
		if (!entity.hasStack() || state.get(PearlHubBlock.BEACON_POWERED)) { return; }
		super.render(entity, tickDelta, matrices, vertexConsumers, light, overlay);
		if (state.get(PearlHubBlock.EXPENDED)) { light /= 2; }
		VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(Identifier.of(Resonance.MOD_ID, "textures/entity/pearl_hub/" + Registries.ITEM.getId(entity.getStack().getItem()).getPath() + ".png")));

		matrices.push();
		this.pearl.setAngles(0, 2.356F - state.get(PearlHubBlock.FACING).asRotation() * MathHelper.RADIANS_PER_DEGREE, 0);
		this.pearl.render(matrices, consumer, light, overlay);
		matrices.pop();
	}

	@Override
	protected Vec3d getOffset(BlockState state, BlockPos pos) {
		return new Vec3d(0.5, 0.75, 0.5);
	}
}