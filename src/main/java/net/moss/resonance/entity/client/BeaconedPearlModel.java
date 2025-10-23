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
import net.moss.resonance.Resonance;
import net.moss.resonance.entity.BeaconedPearlEntity;
import net.moss.resonance.util.PearlStatsComponent;

public class BeaconedPearlModel<T extends BeaconedPearlEntity> extends SinglePartEntityModel<T> {
	private final ModelPart root;
	private final ModelPart pearl;

	public BeaconedPearlModel(ModelPart root) {
		this.root = root.getChild("root");
		this.pearl = this.root.getChild("pearl");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData root = modelPartData.addChild("root", ModelPartBuilder.create(), ModelTransform.pivot(0, 19, 0));
		root.addChild("pearl", ModelPartBuilder.create()
				.uv(0, 12).cuboid(-2.5F, -2.5F, -2.5F, 5, 5, 5, Dilation.NONE)
				.uv(0, 0).cuboid(-3, -3, -3, 6, 6, 6, Dilation.NONE),
				ModelTransform.NONE);
		return TexturedModelData.of(modelData, 32, 32);
	}

	@Override
	public ModelPart getPart() {
		return root;
	}

	@Override
	public void setAngles(BeaconedPearlEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		float left;
		float right;
		switch (entity.getMainHandStack().getOrDefault(Resonance.PEARL_STATS, PearlStatsComponent.DEFAULT).nature()) {
			case AGITATED:
				animationProgress *= animationProgress / MathHelper.PI;
				left = MathHelper.cos(animationProgress);
				right = MathHelper.sin(animationProgress);
				this.pearl.setAngles(left * 2, 0, right);
				this.pearl.setPivot(left, left * 2, right * 2);
				break;
			case PERIODIC:
				left = MathHelper.cos(animationProgress);
				right = MathHelper.sin(animationProgress);
				this.pearl.setAngles(left, 0, right);
				this.pearl.setPivot(left * 4, left, right * 4);
				break;
			case BRILLIANT:
				left = MathHelper.cos(animationProgress) + MathHelper.cos(2 * animationProgress);
				right = MathHelper.sin(animationProgress);
				this.pearl.setAngles(left, 0, right);
				this.pearl.setPivot(0, left, 0);
				break;
			case WHIMSICAL:
				left = MathHelper.cos(animationProgress) + animationProgress - 1;
				this.pearl.setAngles(left, 0, left);
				this.pearl.setPivot(0, 0, 0);
				break;
			default:
				left = MathHelper.cos(animationProgress);
				right = MathHelper.sin(animationProgress);
				this.pearl.setAngles(left, 0, right);
				this.pearl.setPivot(0, 0, 0);
				break;
		}
	}
}