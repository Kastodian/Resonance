package net.moss.resonance.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.moss.resonance.Resonance;
import net.moss.resonance.ResonanceClient;
import net.moss.resonance.entity.YetiCrabEntity;

@Environment(EnvType.CLIENT)
public class YetiCrabRenderer extends MobEntityRenderer<YetiCrabEntity, YetiCrabModel<YetiCrabEntity>> {
	private static final Identifier TEXTURE = Identifier.of(Resonance.MOD_ID, "textures/entity/yeti_crab.png");

	public YetiCrabRenderer(Context context) {
		super(context, new YetiCrabModel<>(context.getPart(ResonanceClient.YETI_CRAB_LAYER)), 0.2f);
		this.addFeature(new YetiCrabHeldItemFeatureRenderer(this, context.getHeldItemRenderer()));
	}

	@Override
	public Identifier getTexture(YetiCrabEntity entity) {
		return TEXTURE;
	}

	public class YetiCrabHeldItemFeatureRenderer extends FeatureRenderer<YetiCrabEntity, YetiCrabModel<YetiCrabEntity>> {
		private final HeldItemRenderer heldItemRenderer;

		public YetiCrabHeldItemFeatureRenderer(FeatureRendererContext<YetiCrabEntity, YetiCrabModel<YetiCrabEntity>> context, HeldItemRenderer heldItemRenderer) {
			super(context);
			this.heldItemRenderer = heldItemRenderer;
		}

		public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, YetiCrabEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
			matrixStack.push();
			
			matrixStack.translate(this.getContextModel().clawL.pivotX / 16.0F, (this.getContextModel().clawL.pivotY + 25) / 16.0F, this.getContextModel().clawL.pivotZ / 16.0F);
			float right = MathHelper.cos(MathHelper.HALF_PI + animationProgress / 30);
			matrixStack.multiply(RotationAxis.POSITIVE_X.rotation(right / 7 - 0.2618F)); // Pitch
			matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(-0.2618F)); // Yaw
			matrixStack.multiply(RotationAxis.POSITIVE_Z.rotation(right * -0.0837F)); // Roll
			
			matrixStack.translate(0.075F, 0, -0.5F);
			matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180)); // Pitch
			matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90)); // Yaw
			matrixStack.scale(0.6F, 0.6F, 0.6F);

			ItemStack itemStack = entity.getEquippedStack(EquipmentSlot.MAINHAND);
			this.heldItemRenderer.renderItem(entity, itemStack, ModelTransformationMode.GROUND, false, matrixStack, vertexConsumerProvider, light);
			matrixStack.pop();
		}
	}
}