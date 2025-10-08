package net.moss.resonance.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.moss.resonance.Resonance;
import net.moss.resonance.entity.VentFluidProjectileEntity;

@Environment(value=EnvType.CLIENT)
public class VentFluidProjectileRenderer extends EntityRenderer<VentFluidProjectileEntity> {
	public static final Identifier TEXTURE = Identifier.of(Resonance.MOD_ID, "textures/entity/vent_fluid_projectile.png");

	public VentFluidProjectileRenderer(EntityRendererFactory.Context context) {
		super(context);
	}

	@Override
	public void render(VentFluidProjectileEntity sculkProjectileEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) { return; }

	@Override
	public Identifier getTexture(VentFluidProjectileEntity dragonFireballEntity) {
		return TEXTURE;
	}
}