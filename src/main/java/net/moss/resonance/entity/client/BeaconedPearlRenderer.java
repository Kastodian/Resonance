package net.moss.resonance.entity.client;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import net.moss.resonance.Resonance;
import net.moss.resonance.ResonanceClient;
import net.moss.resonance.block.renderer.PearlHubBlockEntityRenderer;
import net.moss.resonance.entity.BeaconedPearlEntity;

@Environment(value=EnvType.CLIENT)
public class BeaconedPearlRenderer extends MobEntityRenderer<BeaconedPearlEntity, BeaconedPearlModel<BeaconedPearlEntity>> {
	private static final Identifier TEXTURE = Identifier.of(Resonance.MOD_ID, "textures/entity/lost_soul.png");
    private static final Identifier BEAM_TEXTURE = Identifier.of(Resonance.MOD_ID, "textures/entity/pearl_hub/beam.png");

	public BeaconedPearlRenderer(Context context) {
		super(context, new BeaconedPearlModel<>(context.getPart(ResonanceClient.BEACONED_PEARL_LAYER)), 0);
	}

	@Override
	public Identifier getTexture(BeaconedPearlEntity livingEntity) {
		ItemStack stack = livingEntity.getStackInHand(Hand.MAIN_HAND);
		if (stack.isEmpty()) { return TEXTURE; }
		return PearlHubBlockEntityRenderer.getPearlTexture(stack);
	}

	@Override
	public void render(BeaconedPearlEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        float entityScale = livingEntity.getWidth() * .25f;
        matrixStack.push();
        matrixStack.translate(0, livingEntity.getBoundingBox().getLengthY() / 2, 0);
        float animationProgress = (livingEntity.age + g) / 200.0F;
        Random random = Random.create(432L);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getBeaconBeam(BEAM_TEXTURE, true));

		Rarity rarity = livingEntity.getStackInHand(Hand.MAIN_HAND).getRarity();
		int color = rarity.getFormatting().getColorValue();
		float fadeProgress = (rarity.ordinal() + 1) * (rarity.ordinal() + 1) * (rarity.ordinal() + 1) * .05f;
		int red = ColorHelper.Argb.getRed(color);
		int green = ColorHelper.Argb.getGreen(color);
		int blue = ColorHelper.Argb.getBlue(color);

        float segments = Math.min(animationProgress, .8f);
        for (int j = 0; (float) j < (segments + segments * segments) / 2.0F * 30.0F; ++j) {
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(random.nextFloat() * 360.0F));
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(random.nextFloat() * 360.0F));
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(random.nextFloat() * 360.0F));
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(random.nextFloat() * 360.0F));
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(random.nextFloat() * 360.0F));
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(random.nextFloat() * 360.0F + animationProgress * 90.0F));
            float size1 = (random.nextFloat() * 10.0F + 5.0F + fadeProgress * 5.0F) * entityScale * .4f;
            Matrix4f matrix = matrixStack.peek().getPositionMatrix();
            Matrix3f normalMatrix2 = matrixStack.peek().getNormalMatrix();
			
            drawTriangle(vertexConsumer, matrix, normalMatrix2, size1, red, green, blue);
        }
        matrixStack.pop();
		super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, LightmapTextureManager.MAX_LIGHT_COORDINATE);
	}

	@Override
	protected float getAnimationProgress(BeaconedPearlEntity entity, float tickDelta) {
		return MathHelper.wrapDegrees(super.getAnimationProgress(entity, tickDelta)) * MathHelper.RADIANS_PER_DEGREE * 2;
	}

	private static void drawTriangle(VertexConsumer consumer, Matrix4f poseMatrix, Matrix3f normalMatrix, float size, int red, int green, int blue) {
        consumer.vertex(poseMatrix, 0, 0, 0).color(255, 255, 255, 255).texture(0f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0f, 1f, 0f);
        consumer.vertex(poseMatrix, 0, 3 * size, -1 * size).color(red, green, blue, 0).texture(0f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0f, 1f, 0f);
        consumer.vertex(poseMatrix, 0, 3 * size, 1 * size).color(red, green, blue, 0).texture(1f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0f, 1f, 0f);
        consumer.vertex(poseMatrix, 0, 0, 0).color(255, 255, 255, 255).texture(1f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0f, 1f, 0f);
    }
}