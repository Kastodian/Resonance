package net.moss.resonance.world;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.gl.VertexBuffer.Usage;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import net.moss.resonance.Resonance;

public class DreamSweetSkyRenderer implements DimensionRenderingRegistry.SkyRenderer {
	private VertexBuffer starsBuffer;
	private static final Identifier MOON_PHASES = Identifier.of("textures/environment/moon_phases.png");
	private static final Identifier HORIZON = Identifier.of(Resonance.MOD_ID, "textures/environment/dream_sweet/horizon.png");

	private void renderStars() {
		if (this.starsBuffer != null) { this.starsBuffer.close(); }
		this.starsBuffer = new VertexBuffer(Usage.STATIC);
		this.starsBuffer.bind();
		this.starsBuffer.upload(this.renderStars(Tessellator.getInstance()));
		VertexBuffer.unbind();
	}

	private BuiltBuffer renderStars(Tessellator tessellator) {
		Random random = Random.create(10842L);
		BufferBuilder bufferBuilder = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION);

		for(int j = 0; j < 1500; ++j) {
			float g = random.nextFloat() * 2.0F - 1.0F;
			float h = random.nextFloat() * 2.0F - 1.0F;
			float k = random.nextFloat() * 2.0F - 1.0F;
			float l = 0.15F + random.nextFloat() * 0.1F;
			float m = MathHelper.magnitude(g, h, k);
			if (!(m <= 0.010000001F) && !(m >= 1.0F)) {
				Vector3f vector3f = (new Vector3f(g, h, k)).normalize(100.0F);
				float n = (float)(random.nextDouble() * 3.1415927410125732 * 2.0);
				Quaternionf quaternionf = (new Quaternionf()).rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), vector3f).rotateZ(n);
				bufferBuilder.vertex(vector3f.add((new Vector3f(l, -l, 0.0F)).rotate(quaternionf)));
				bufferBuilder.vertex(vector3f.add((new Vector3f(l, l, 0.0F)).rotate(quaternionf)));
				bufferBuilder.vertex(vector3f.add((new Vector3f(-l, l, 0.0F)).rotate(quaternionf)));
				bufferBuilder.vertex(vector3f.add((new Vector3f(-l, -l, 0.0F)).rotate(quaternionf)));
			}
		}
		return bufferBuilder.end();
	}

	@Override
	public void render(WorldRenderContext context) {
		MatrixStack matrixStack = new MatrixStack();
		matrixStack.multiplyPositionMatrix(context.positionMatrix());
		//BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		RenderSystem.depthMask((boolean)false);

		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ONE, DstFactor.ZERO);

		this.renderStars();
		starsBuffer.bind();
		starsBuffer.draw(matrixStack.peek().getPositionMatrix(), context.projectionMatrix(), GameRenderer.getPositionProgram());
		VertexBuffer.unbind();

		matrixStack.push();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1);
		Matrix4f matrix4f3 = matrixStack.peek().getPositionMatrix();
		matrixStack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(-100.0F));
		matrixStack.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(20.0F));
		float k = 150.0F;
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderTexture(0, MOON_PHASES);
		//bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder2 = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

		bufferBuilder2.vertex(matrix4f3, -k, -100.0F, k).texture(0f, 0f);
		bufferBuilder2.vertex(matrix4f3, k, -100.0F, k).texture(0.25f, 0f);
		bufferBuilder2.vertex(matrix4f3, k, -100.0F, -k).texture(0.25f, 0.5f);
		bufferBuilder2.vertex(matrix4f3, -k, -100.0F, -k).texture(0f, 0.5f);
		BufferRenderer.drawWithGlobalProgram(bufferBuilder2.end());


		//matrixStack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(100.0F));
		matrixStack.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(90.0F));
		RenderSystem.setShaderTexture(0, HORIZON);
		//bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		bufferBuilder2 = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		bufferBuilder2.vertex(matrix4f3, -2 * k, -100.0F, k).texture(0f, 0f);
		bufferBuilder2.vertex(matrix4f3, 2 * k, -100.0F, k).texture(1f, 0f);
		bufferBuilder2.vertex(matrix4f3, 2 * k, -100.0F, -k).texture(1f, 1f);
		bufferBuilder2.vertex(matrix4f3, -2 *k, -100.0F, -k).texture(0f, 1f);
		BufferRenderer.drawWithGlobalProgram(bufferBuilder2.end());


		RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
		matrixStack.pop();
		RenderSystem.depthMask((boolean)true);
	}
}