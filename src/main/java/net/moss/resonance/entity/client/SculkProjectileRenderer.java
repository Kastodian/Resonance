package net.moss.resonance.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.moss.resonance.Resonance;
import net.moss.resonance.entity.SculkProjectileEntity;

@Environment(value=EnvType.CLIENT)
public class SculkProjectileRenderer extends EntityRenderer<SculkProjectileEntity> {
	public static final Identifier TEXTURE = Identifier.of(Resonance.MOD_ID, "textures/entity/sculk_projectile.png");

	public SculkProjectileRenderer(EntityRendererFactory.Context context) {
		super(context);
	}

	@Override
	public void render(SculkProjectileEntity sculkProjectileEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) { return; }

	@Override
	public Identifier getTexture(SculkProjectileEntity dragonFireballEntity) {
		return TEXTURE;
	}
}