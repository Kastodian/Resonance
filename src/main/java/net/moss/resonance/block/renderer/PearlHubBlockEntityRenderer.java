package net.moss.resonance.block.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.moss.resonance.Resonance;
import net.moss.resonance.block.PearlHubBlock;
import net.moss.resonance.block.entity.PearlHubBlockEntity;
import net.moss.resonance.util.abstracts.BlockEntityWithNametagRenderer;

@Environment(value=EnvType.CLIENT)
public class PearlHubBlockEntityRenderer extends BlockEntityWithNametagRenderer<PearlHubBlockEntity> {
	private final ModelPart pearl;
	private final float height = 7.01f;

	public PearlHubBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
		super(context);
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData pearl = modelPartData.addChild("pearl", ModelPartBuilder.create().uv(0, 0).cuboid(-3, height, -3, 6, 6, 6, Dilation.NONE).uv(0, 12).cuboid(-2.5f, height + 0.25f, -2.5f, 5, 5, 5, Dilation.NONE), ModelTransform.of(8, 0, 8, 0, 0, 0));
		this.pearl = pearl.createPart(32, 32);
	}

	public static Identifier getPearlTexture(ItemStack stack) {
		String shiny = stack.contains(Resonance.PEARL_STATS) && stack.get(Resonance.PEARL_STATS).shiny() ? "_shiny" : "";
		return Identifier.of(Resonance.MOD_ID, "textures/entity/pearl_hub/" + Registries.ITEM.getId(stack.getItem()).getPath() + shiny + ".png");
	}

	@Override
	public void render(PearlHubBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		BlockState state = entity.getCachedState();
		if (!entity.hasStack() || state.get(PearlHubBlock.BEACON_POWERED)) { return; }
		super.render(entity, tickDelta, matrices, vertexConsumers, light, overlay);
		if (state.get(PearlHubBlock.EXPENDED)) { light /= 2; }
		VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(getPearlTexture(entity.getStack())));

		matrices.push();
		this.pearl.setAngles(0, 2.356F - state.get(PearlHubBlock.FACING).asRotation() * MathHelper.RADIANS_PER_DEGREE, 0);
		this.pearl.render(matrices, consumer, light, overlay);

		if (entity.getStack().hasGlint()) {
			this.pearl.render(matrices, vertexConsumers.getBuffer(RenderLayer.getDirectEntityGlint()), light * 2, OverlayTexture.DEFAULT_UV);
		}
		matrices.pop();
	}

	@Override
	protected Vec3d getOffset(BlockState state, BlockPos pos) {
		return new Vec3d(0.5, 0.75, 0.5);
	}

	@Override
	protected boolean hasLabel(PearlHubBlockEntity entity) {
		return entity.hasStack() && entity.getStack().contains(DataComponentTypes.CUSTOM_NAME) && super.hasLabel(entity);
	}

	@Override
	protected Text getText(PearlHubBlockEntity entity) {
		return entity.getStack().getName();
	}
}