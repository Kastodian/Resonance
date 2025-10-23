package net.moss.resonance.entity.client;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;
import net.moss.resonance.entity.LostSoulEntity;

public class LostSoulModel<T extends LostSoulEntity> extends SinglePartEntityModel<T> {
	private final ModelPart root;
	private final ModelPart outer;
	private final ModelPart inner;
	private final ModelPart core;

	public LostSoulModel(ModelPart root) {
		this.root = root.getChild("root");
		this.outer = this.root.getChild("outer");
		this.inner = this.root.getChild("inner");
		this.core = this.root.getChild("core");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData root = modelPartData.addChild("root", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 21.0F, 0.0F));
		root.addChild("outer", ModelPartBuilder.create().uv(0, 14).cuboid(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, Dilation.NONE), ModelTransform.NONE);
		root.addChild("inner", ModelPartBuilder.create().uv(0, 4).cuboid(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, Dilation.NONE), ModelTransform.NONE);
		root.addChild("core", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, Dilation.NONE), ModelTransform.NONE);
		return TexturedModelData.of(modelData, 32, 32);
	}

	@Override
	public ModelPart getPart() {
		return root;
	}

	@Override
	public void setAngles(LostSoulEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		animationProgress /= 10;
		float left = MathHelper.cos(animationProgress);
		float right = MathHelper.cos(MathHelper.HALF_PI + animationProgress);
		this.outer.setAngles(left, 0, right);
		this.inner.setAngles(right, 0, left);
		this.core.setAngles(-right, 0, -left);
	}
}