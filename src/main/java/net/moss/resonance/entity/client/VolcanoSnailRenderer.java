package net.moss.resonance.entity.client;

import java.util.Map;
import com.google.common.collect.Maps;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.moss.resonance.Resonance;
import net.moss.resonance.ResonanceClient;
import net.moss.resonance.entity.VolcanoSnailEntity;

public class VolcanoSnailRenderer extends MobEntityRenderer<VolcanoSnailEntity, VolcanoSnailModel<VolcanoSnailEntity>> {
	private static final Map<VolcanoSnailEntity.Type, Identifier> TEXTURES = Util.make(Maps.<VolcanoSnailEntity.Type, Identifier>newHashMap(), map -> {
		map.put(VolcanoSnailEntity.Type.IRON, Identifier.of(Resonance.MOD_ID, "textures/entity/volcano_snail_iron.png"));
		map.put(VolcanoSnailEntity.Type.COPPER, Identifier.of(Resonance.MOD_ID, "textures/entity/volcano_snail_copper.png"));
	});
    
    public VolcanoSnailRenderer(Context context) {
        super(context, new VolcanoSnailModel<>(context.getPart(ResonanceClient.VOCANO_SNAIL_LAYER)), 0.2f);
    }

    @Override
    public Identifier getTexture(VolcanoSnailEntity entity) {
		return (Identifier)TEXTURES.get(entity.getVariant());
    }
}