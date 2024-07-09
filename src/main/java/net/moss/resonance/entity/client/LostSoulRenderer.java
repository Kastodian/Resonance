package net.moss.resonance.entity.client;

import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import net.moss.resonance.Resonance;
import net.moss.resonance.ResonanceClient;
import net.moss.resonance.entity.LostSoulEntity;

public class LostSoulRenderer extends MobEntityRenderer<LostSoulEntity, LostSoulModel<LostSoulEntity>> {
    private static final Identifier TEXTURE = Identifier.of(Resonance.MOD_ID, "textures/entity/lost_soul.png");
    
    public LostSoulRenderer(Context context) {
        super(context, new LostSoulModel<>(context.getPart(ResonanceClient.LOST_SOUL_LAYER)), 0);
    }

    @Override
    public Identifier getTexture(LostSoulEntity entity) {
        return TEXTURE;
    }
}