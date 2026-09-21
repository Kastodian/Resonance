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
import net.moss.resonance.entity.VolcanoSnailEntity;

public class VolcanoSnailModel<T extends VolcanoSnailEntity> extends SinglePartEntityModel<T> {
	private final ModelPart root;
	private final ModelPart stalkR;
	private final ModelPart stalkL;
	private final ModelPart shell;

	public VolcanoSnailModel(ModelPart root) {
		this.root = root.getChild("root");
		this.stalkR = this.root.getChild("stalk_r");
		this.stalkL = this.root.getChild("stalk_l");
		this.shell = this.root.getChild("shell");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData root = modelPartData.addChild("root", ModelPartBuilder.create().uv(0, 11).cuboid(-1.5F, -2, -3, 3, 2, 6, Dilation.NONE), ModelTransform.pivot(0, 24, 0));
		root.addChild("stalk_r", ModelPartBuilder.create().uv(18, 13).cuboid(-0.5F, 0, 0, 1, 2, 0, Dilation.NONE), ModelTransform.of(-1.5F, -2, -3, -0.3491F, 0.3491F, 0));
		root.addChild("stalk_l", ModelPartBuilder.create().uv(18, 11).cuboid(-0.5F, 0, 0, 1, 2, 0, Dilation.NONE), ModelTransform.of(1.5F, -2, -3, -0.3491F, -0.3491F, 0));
		root.addChild("shell", ModelPartBuilder.create().uv(0, 0).cuboid(-2, -5, -6, 4, 5, 6, Dilation.NONE), ModelTransform.of(0, 0, 3, -0.2618F, 0, 0));
		return TexturedModelData.of(modelData, 32, 32);
	}

	@Override
	public ModelPart getPart() {
		return root;
	}

	@Override
	public void setAngles(VolcanoSnailEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		animationProgress /= 10;
		float left = MathHelper.cos(animationProgress);
		float right = MathHelper.cos(MathHelper.HALF_PI + animationProgress);
		this.stalkL.setAngles(left / 6 - 0.3491F, -0.3491F, left * -0.0837F);
		this.stalkR.setAngles(right / 6 - 0.3491F, 0.3491F, right * 0.0837F);
		if (entity.getHungry()) {
			this.shell.pitch = 0;
		} else {
			this.shell.pitch = -0.2618F;
		}
	}
}