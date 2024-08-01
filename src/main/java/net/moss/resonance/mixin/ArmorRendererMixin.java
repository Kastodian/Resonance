package net.moss.resonance.mixin;

import java.util.List;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper.Argb;
import net.moss.resonance.Resonance;
import net.moss.resonance.util.Utils;
import net.moss.resonance.util.interfaces.ScarfInterface;

@Environment(EnvType.CLIENT)
@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, BEModel extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
    public ArmorRendererMixin(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Shadow protected static Map<String, Identifier> ARMOR_TEXTURE_CACHE;
    @Shadow private BEModel getModel(EquipmentSlot slot) { return null; }
    @Shadow protected abstract void setVisible(BEModel model, EquipmentSlot chest);
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Inject(method = "render", at = @At("TAIL"), cancellable = true)
	private void renderNecklace(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        BEModel model = this.getModel(EquipmentSlot.CHEST);
        List<ItemStack> equipped = Utils.getAllEquipped(livingEntity, false);
        for (ItemStack item : equipped) {
            if (item.getItem() instanceof ScarfInterface necklaceTrinket) {
                this.setVisible(model, EquipmentSlot.CHEST);
                ((BipedEntityModel)this.getContextModel()).copyBipedStateTo(model);
                int special = necklaceTrinket.getSpecial(item);
                int i = item.isIn(ItemTags.DYEABLE) ? Argb.fullAlpha(DyedColorComponent.getColor(item, 16777215)) : -1;
                String texture = necklaceTrinket.getTexture() + (special > 0 ? ("_special_" + special) : "") + ".png";
                VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getArmorCutoutNoCull(Identifier.of(texture)));
                model.render(matrixStack, vertexConsumer, light, OverlayTexture.DEFAULT_UV, i);
                if (item.hasGlint()) {
                    model.render(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.getArmorEntityGlint()), light, OverlayTexture.DEFAULT_UV);
                }
            }
        }
	}

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void phantomScarf(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
		if (!livingEntity.isInvisible()) { return; }
		if (Utils.isItemEquipped(livingEntity, Resonance.PHANTOM_SCARF, false)) {
            ci.cancel();
		}
	}
}