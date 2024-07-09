package net.moss.resonance.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.util.Identifier;
import net.moss.resonance.Resonance;
import net.moss.resonance.entity.WhistlingArrowEntity;

@Environment(value=EnvType.CLIENT)
public class WhistlingArrowRenderer
extends ProjectileEntityRenderer<WhistlingArrowEntity> {
    public static final Identifier TEXTURE = Identifier.of(Resonance.MOD_ID, "textures/entity/whistling_arrow.png");

    public WhistlingArrowRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(WhistlingArrowEntity whistlingArrowEntity) {
        return TEXTURE;
    }
}

