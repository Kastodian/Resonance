package net.moss.resonance.mixin;

import net.minecraft.client.render.entity.EntityRendererFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.moss.resonance.ExtraFeatureRenderer;
import net.moss.resonance.Resonance;

@Environment(EnvType.CLIENT)
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

	public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
		super(ctx, model, shadowRadius);
	}

	@Inject(method = "<init>", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void initMixin(EntityRendererFactory.Context ctx, boolean slim, CallbackInfo info) {
		this.addFeature(new ExtraFeatureRenderer(this, ctx.getHeldItemRenderer()));
	}

	@Inject(at = @At(value = "HEAD"), method = "getArmPose", cancellable = true)
	private static void getArmPose(AbstractClientPlayerEntity player, Hand hand, CallbackInfoReturnable<BipedEntityModel.ArmPose> callbackinfo) {
		Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
		ItemStack thisStack = player.getStackInHand(hand);
		ItemStack otherStack = player.getStackInHand(otherHand);
		if (!(thisStack.isEmpty() ^ otherStack.isEmpty()) || !(thisStack.isIn(Resonance.DUAL_WIELD_RENDER_TAG) || otherStack.isIn(Resonance.DUAL_WIELD_RENDER_TAG))) { return; }
		callbackinfo.setReturnValue(BipedEntityModel.ArmPose.ITEM);
	}
}