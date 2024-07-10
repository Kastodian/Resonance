package net.moss.resonance;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class ExtraFeatureRenderer extends HeldItemFeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    private final HeldItemRenderer heldItemRenderer;

    public ExtraFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context, HeldItemRenderer heldItemRenderer) {
        super(context, heldItemRenderer);
        this.heldItemRenderer = heldItemRenderer;
    }

    // TODO: TRINKETS SUPPORT
    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, AbstractClientPlayerEntity livingEntity, float f, float g, float h, float j, float k, float l) {
        if (!(livingEntity instanceof AbstractClientPlayerEntity)) { return; }
        PlayerEntity player = (PlayerEntity) livingEntity;
        ItemStack chestStack = player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack offHandStack = player.getEquippedStack(EquipmentSlot.OFFHAND);
        ItemStack mainHandStack = player.getEquippedStack(EquipmentSlot.MAINHAND);
        if (!chestStack.isEmpty() && chestStack.isIn(Resonance.RENDER_ON_BACK_TAG)) {
            matrixStack.push();
            ModelPart modelPart = this.getContextModel().body;
            modelPart.rotate(matrixStack);
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
            matrixStack.translate(0, 0.25, 0);
            matrixStack.scale(0.65f, 0.65f, 0.65f);
            heldItemRenderer.renderItem(livingEntity, chestStack, ModelTransformationMode.HEAD, false, matrixStack, vertexConsumerProvider, i);
            matrixStack.pop();
        }
        ItemStack tempStack = ItemStack.EMPTY;
        if (!mainHandStack.isEmpty() && mainHandStack.isIn(Resonance.DUAL_WIELD_RENDER_TAG) && (offHandStack.isEmpty())) { tempStack = mainHandStack; }
        else if (!offHandStack.isEmpty() && offHandStack.isIn(Resonance.DUAL_WIELD_RENDER_TAG) && (mainHandStack.isEmpty())) { tempStack = offHandStack; }
        if (!tempStack.isEmpty()) {
            matrixStack.push();
            this.renderItem(livingEntity, tempStack, ModelTransformationMode.THIRD_PERSON_RIGHT_HAND, Arm.RIGHT, matrixStack, vertexConsumerProvider, i);
            this.renderItem(livingEntity, tempStack, ModelTransformationMode.THIRD_PERSON_LEFT_HAND, Arm.LEFT, matrixStack, vertexConsumerProvider, i);
            matrixStack.pop();
        }
    }
}