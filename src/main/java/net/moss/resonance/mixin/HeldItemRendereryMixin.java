package net.moss.resonance.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.moss.resonance.Resonance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(HeldItemRenderer.class)
public class HeldItemRendereryMixin {
	@Shadow private void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress) { }
	@Shadow private void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress) { }

	@Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
	private void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		if (!item.isEmpty()) { return; }

		boolean bl = hand == Hand.MAIN_HAND;
		Arm arm = bl ? player.getMainArm() : player.getMainArm().getOpposite();
		Hand otherHand = bl ? Hand.OFF_HAND : Hand.MAIN_HAND;
		ItemStack otherStack = player.getStackInHand(otherHand);

		if (otherStack.isEmpty() || !otherStack.isIn(Resonance.DUAL_WIELD_RENDER_TAG)) { return; }
		HeldItemRenderer heldItemRenderer = (HeldItemRenderer)(Object) this;
		matrices.push();
		boolean bl2 = arm == Arm.RIGHT;
		float n = -0.4f * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float)Math.PI);
		float m = 0.2f * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2));
		float f = -0.2f * MathHelper.sin(swingProgress * (float)Math.PI);
		int o = bl2 ? 1 : -1;
		matrices.translate(o * n, m, f);
		this.applyEquipOffset(matrices, arm, equipProgress);
		this.applySwingOffset(matrices, arm, swingProgress);
		heldItemRenderer.renderItem(player, otherStack, bl2 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !bl2, matrices, vertexConsumers, light);
		matrices.pop();
		ci.cancel();
	}
}