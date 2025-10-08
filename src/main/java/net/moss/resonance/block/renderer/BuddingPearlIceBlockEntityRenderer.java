package net.moss.resonance.block.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import net.moss.resonance.block.entity.BuddingPearlIceBlockEntity;

@Environment(value=EnvType.CLIENT)
public class BuddingPearlIceBlockEntityRenderer implements BlockEntityRenderer<BuddingPearlIceBlockEntity> {
	private final ItemRenderer itemRenderer;

	public BuddingPearlIceBlockEntityRenderer(BlockEntityRendererFactory.Context context) { this.itemRenderer = context.getItemRenderer(); }

	@Override
	public void render(BuddingPearlIceBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		World world = entity.getWorld();
		if (world == null) { return; }
		if (entity.hasSeed()) {
			matrices.push();
			matrices.translate(0.25, 0.5, 0.4);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30));
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
			matrices.scale(0.5f, 0.5f, 0.5f);
			this.itemRenderer.renderItem(entity.getSeed(), ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, world, 0);
			matrices.pop();
		} if (entity.hasPotion()) {
			matrices.push();
			matrices.translate(0.75, 0.5, 0.6);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-30));
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-30));
			matrices.scale(0.5f, 0.5f, 0.5f);
			this.itemRenderer.renderItem(entity.getPotion(), ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, world, 0);
			matrices.pop();
		} if (entity.hasCatalyst()) {
			matrices.push();
			matrices.translate(0.4, 0.5, 0.75);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-60));
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(60));
			matrices.scale(0.5f, 0.5f, 0.5f);
			this.itemRenderer.renderItem(entity.getCatalyst(), ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, world, 0);
			matrices.pop();
		} if (entity.hasPearl()) {
			int k = entity.getCachedState().get(Properties.DUSTED);
			if (k <= 0) { return; }
			Direction direction = entity.getHitDirection();
			if (direction == null) { return; }
			matrices.push();
			matrices.translate(0, 0.5, 0);
			float[] fs = this.getTranslation(direction, k);
			matrices.translate(fs[0], fs[1], fs[2]);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(75.0f));
			boolean bl = direction == Direction.EAST || direction == Direction.WEST;
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((bl ? 90 : 0) + 11));
			matrices.scale(0.5f, 0.5f, 0.5f);
			this.itemRenderer.renderItem(entity.getPearl(), ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, world, 0);
			matrices.pop();
		}
	}

	private float[] getTranslation(Direction direction, int dustedLevel) {
		float[] fs = new float[]{0.5f, 0.0f, 0.5f};
		float f = dustedLevel / 10.0f * 0.75f;
		switch (direction) {
			case EAST: {
				fs[0] = 0.73f + f;
				break;
			} case WEST: {
				fs[0] = 0.25f - f;
				break;
			} case UP: {
				fs[1] = 0.25f + f;
				break;
			} case DOWN: {
				fs[1] = -0.23f - f;
				break;
			} case NORTH: {
				fs[2] = 0.25f - f;
				break;
			} case SOUTH: {
				fs[2] = 0.73f + f;
			}
		}
		return fs;
	}
}