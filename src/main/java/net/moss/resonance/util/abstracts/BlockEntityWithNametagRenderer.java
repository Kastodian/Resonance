package net.moss.resonance.util.abstracts;

import org.joml.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public abstract class BlockEntityWithNametagRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
	protected final EntityRenderDispatcher dispatcher;
	protected final TextRenderer textRenderer;

	public BlockEntityWithNametagRenderer(BlockEntityRendererFactory.Context ctx) {
		this.dispatcher = ctx.getEntityRenderDispatcher();
		this.textRenderer = ctx.getTextRenderer();
	}

	protected boolean renderLabel(T entity, Vec3d offsetPos, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		if (!hasLabel(entity)) { return false; }
		Text text = this.getText(entity);
		matrices.push();
		matrices.translate(offsetPos.x, offsetPos.y + 0.5, offsetPos.z);
		matrices.multiply(this.dispatcher.getRotation());
		matrices.scale(0.025F, -0.025F, 0.025F);
		Matrix4f matrix4f = matrices.peek().getPositionMatrix();
		float g = -this.textRenderer.getWidth(text) / 2;
		this.textRenderer.draw(text, g, 0, 553648127, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, (int)(MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F) * 255.0F) << 24, light);
		this.textRenderer.draw(text, g, 0, -1, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
		matrices.pop();
		return true;
	}

	@Override public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) { this.renderLabel(entity, getOffset(entity.getCachedState(), entity.getPos()), tickDelta, matrices, vertexConsumers, light); }
	protected Vec3d getOffset(BlockState state, BlockPos pos) { return new Vec3d(0.5, 0.5, 0.5); }
	protected boolean hasLabel(T entity) { return MinecraftClient.isHudEnabled() && MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult hitResult && hitResult.getBlockPos().equals(entity.getPos()); }
	protected abstract Text getText(T entity);
}