package net.moss.resonance.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.moss.resonance.Resonance;
import net.moss.resonance.entity.SculkProjectileEntity;

@Environment(value=EnvType.CLIENT)
public class SculkProjectileRenderer extends EntityRenderer<SculkProjectileEntity> {
    public static final Identifier TEXTURE = Identifier.of(Resonance.MOD_ID, "textures/entity/sculk_projectile.png");
    private static final RenderLayer LAYER = RenderLayer.getEntityCutoutNoCull(TEXTURE);

    public SculkProjectileRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(SculkProjectileEntity sculkProjectileEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        matrixStack.scale(.5f, .5f, .5f);
        matrixStack.multiply(this.dispatcher.getRotation());
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        MatrixStack.Entry entry = matrixStack.peek();
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(LAYER);
        SculkProjectileRenderer.produceVertex(vertexConsumer, entry, i, 0.0f, 0, 0, 1);
        SculkProjectileRenderer.produceVertex(vertexConsumer, entry, i, 1.0f, 0, 1, 1);
        SculkProjectileRenderer.produceVertex(vertexConsumer, entry, i, 1.0f, 1, 1, 0);
        SculkProjectileRenderer.produceVertex(vertexConsumer, entry, i, 0.0f, 1, 0, 0);
        matrixStack.pop();
        super.render(sculkProjectileEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    private static void produceVertex(VertexConsumer vertexConsumer, MatrixStack.Entry normalMatrix, int light, float x, int y, int textureU, int textureV) {
        vertexConsumer.vertex(normalMatrix, x - 0.5f, (float)y - 0.25f, 0.0f).color(255, 255, 255, 255).texture(textureU, textureV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normalMatrix, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public Identifier getTexture(SculkProjectileEntity dragonFireballEntity) {
        return TEXTURE;
    }
}