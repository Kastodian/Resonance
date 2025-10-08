package net.moss.resonance.entity.client;

import java.util.Map;
import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.moss.resonance.Resonance;
import net.moss.resonance.entity.VolcanoSnailEntity;

@Environment(EnvType.CLIENT)
public class VolcanoSnailDyeFeatureRenderer extends FeatureRenderer<VolcanoSnailEntity, VolcanoSnailModel<VolcanoSnailEntity>> {
	private static final Map<DyeColor, Identifier> TEXTURES = Util.make(Maps.newEnumMap(DyeColor.class), map -> {
		map.put(DyeColor.WHITE, Identifier.of(Resonance.MOD_ID, "textures/entity/volcano_snail/dye/white.png"));
		map.put(DyeColor.ORANGE, Identifier.of(Resonance.MOD_ID, "textures/entity/volcano_snail/dye/orange.png"));
		map.put(DyeColor.MAGENTA, Identifier.of(Resonance.MOD_ID, "textures/entity/volcano_snail/dye/magenta.png"));
		map.put(DyeColor.LIGHT_BLUE, Identifier.of(Resonance.MOD_ID, "textures/entity/volcano_snail/dye/light_blue.png"));
		map.put(DyeColor.YELLOW, Identifier.of(Resonance.MOD_ID, "textures/entity/volcano_snail/dye/yellow.png"));
		map.put(DyeColor.LIME, Identifier.of(Resonance.MOD_ID, "textures/entity/volcano_snail/dye/lime.png"));
		map.put(DyeColor.PINK, Identifier.of(Resonance.MOD_ID, "textures/entity/volcano_snail/dye/pink.png"));
		map.put(DyeColor.GRAY, Identifier.of(Resonance.MOD_ID, "textures/entity/volcano_snail/dye/gray.png"));
		map.put(DyeColor.LIGHT_GRAY, Identifier.of(Resonance.MOD_ID, "textures/entity/volcano_snail/dye/light_gray.png"));
		map.put(DyeColor.CYAN, Identifier.of(Resonance.MOD_ID, "textures/entity/volcano_snail/dye/cyan.png"));
		map.put(DyeColor.PURPLE, Identifier.of(Resonance.MOD_ID, "textures/entity/volcano_snail/dye/purple.png"));
		map.put(DyeColor.BLUE, Identifier.of(Resonance.MOD_ID, "textures/entity/volcano_snail/dye/blue.png"));
		map.put(DyeColor.BROWN, Identifier.of(Resonance.MOD_ID, "textures/entity/volcano_snail/dye/brown.png"));
		map.put(DyeColor.GREEN, Identifier.of(Resonance.MOD_ID, "textures/entity/volcano_snail/dye/green.png"));
		map.put(DyeColor.RED, Identifier.of(Resonance.MOD_ID, "textures/entity/volcano_snail/dye/red.png"));
		map.put(DyeColor.BLACK, Identifier.of(Resonance.MOD_ID, "textures/entity/volcano_snail/dye/black.png"));
	});

	public VolcanoSnailDyeFeatureRenderer(FeatureRendererContext<VolcanoSnailEntity, VolcanoSnailModel<VolcanoSnailEntity>> context) {
		super(context);
	}

	public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, VolcanoSnailEntity volcanoSnailEntity, float f, float g, float h, float j, float k, float l) {
		Identifier identifier = TEXTURES.get(volcanoSnailEntity.getColor());
		if (identifier != null && !volcanoSnailEntity.isInvisible()) {
			this.getContextModel().render(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(identifier)), i, LivingEntityRenderer.getOverlay(volcanoSnailEntity, 0.0F));
		}
	}
}