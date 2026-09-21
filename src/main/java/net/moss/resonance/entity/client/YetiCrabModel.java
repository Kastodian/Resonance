package net.moss.resonance.entity.client;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.math.MathHelper;
import net.moss.resonance.entity.YetiCrabEntity;

public class YetiCrabModel<T extends YetiCrabEntity> extends SinglePartEntityModel<T> {
	public final ModelPart root;
	public final ModelPart clawL;
	public final ModelPart clawR;
	public final ModelPart stalkL;
	public final ModelPart stalkR;
	public final ModelPart legL1;
	public final ModelPart legL2;
	public final ModelPart legL3;
	public final ModelPart legR1;
	public final ModelPart legR2;
	public final ModelPart legR3;
	
	public YetiCrabModel(ModelPart root) {
		this.root = root.getChild("root");
		this.clawL = this.root.getChild("claw_l");
		this.clawR = this.root.getChild("claw_r");
		this.stalkL = this.root.getChild("stalk_l");
		this.stalkR = this.root.getChild("stalk_r");
		this.legL1 = this.root.getChild("leg_l1");
		this.legL2 = this.root.getChild("leg_l2");
		this.legL3 = this.root.getChild("leg_l3");
		this.legR1 = this.root.getChild("leg_r1");
		this.legR2 = this.root.getChild("leg_r2");
		this.legR3 = this.root.getChild("leg_r3");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData root = modelPartData.addChild("root", ModelPartBuilder.create().uv(0, 7).cuboid(-2, -5, -1, 4, 2, 2)
		.uv(0, 0).cuboid(-2.5F, -5.5F, 1, 5, 3, 4), ModelTransform.pivot(0, 25, 0));

		root.addChild("leg_l1", ModelPartBuilder.create().uv(6, 19).cuboid(0, -1, 0, 3, 4, 0), ModelTransform.pivot(2, -4, 1.5F));
		root.addChild("leg_l2", ModelPartBuilder.create().uv(12, 19).cuboid(0, -1, 0, 3, 4, 0), ModelTransform.pivot(2, -4, 3));
		root.addChild("leg_l3", ModelPartBuilder.create().uv(0, 19).cuboid(0, -1, 0, 3, 4, 0), ModelTransform.pivot(2, -4, 4.5F));
		root.addChild("leg_r1", ModelPartBuilder.create().uv(0, 19).cuboid(-3, -1, 0, 3, 4, 0, true), ModelTransform.pivot(-2, -4, 1.5F));
		root.addChild("leg_r2", ModelPartBuilder.create().uv(6, 19).cuboid(-3, -1, 0, 3, 4, 0, true), ModelTransform.pivot(-2, -4, 3));
		root.addChild("leg_r3", ModelPartBuilder.create().uv(12, 19).cuboid(-3, -1, 0, 3, 4, 0, true), ModelTransform.pivot(-2, -4, 4.5F));

		root.addChild("stalk_r", ModelPartBuilder.create().uv(0, 7).cuboid(0, -1, -5, 0, 1, 5), ModelTransform.of(-1, -4, -1, -0.2618F, 0, 0));
		root.addChild("stalk_l", ModelPartBuilder.create().uv(0, 6).cuboid(0, -1, -5, 0, 1, 5), ModelTransform.of(1, -4, -1, -0.2618F, 0, 0));
		root.addChild("claw_r", ModelPartBuilder.create().uv(12, 0).cuboid(-3, -1, -7, 3, 2, 7)
		.uv(10, 9).cuboid(-3.5F, -1.5F, -7, 4, 3, 7, new Dilation(-0.25F))
		.uv(9, 12).cuboid(-2.5F, 0, -8, 1, 0, 1)
		.uv(11, 12).cuboid(-1, 0, -8, 1, 0, 1), ModelTransform.of(-1, -3.5F, -1, 0.2618F, 0.2618F, 0));
		root.addChild("claw_l", ModelPartBuilder.create().uv(12, 0).mirrored().cuboid(0, -1, -7, 3, 2, 7)
		.uv(10, 9).cuboid(-0.5F, -1.5F, -7, 4, 3, 7, new Dilation(-0.25F))
		.uv(9, 11).cuboid(0, 0, -8, 1, 0, 1)
		.uv(11, 11).cuboid(1.5F, 0, -8, 1, 0, 1), ModelTransform.of(1, -3.5F, -1, 0.2618F, -0.2618F, 0));
		return TexturedModelData.of(modelData, 32, 32);
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
		this.root.render(matrices, vertices, light, overlay, color);
	}

	@Override
	public ModelPart getPart() {
		return root;
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		this.legR1.roll = MathHelper.cos(4 * limbAngle + (float)Math.PI) * 2 * limbDistance;
		this.legL1.roll = MathHelper.cos(4 * limbAngle) * 2 * limbDistance;
		this.legR2.roll = this.legL1.roll;
		this.legL2.roll = this.legR1.roll;
		this.legR3.roll = this.legL2.roll;
		this.legL3.roll = this.legR2.roll;

		if (!entity.isWet()) { return; }
		animationProgress /= 30;
		float left = MathHelper.cos(animationProgress * 0.95F);
		float right = MathHelper.cos(MathHelper.HALF_PI + animationProgress);
		this.stalkL.setAngles(left / 6 - 0.1309F, 0, left * -0.0837F);
		this.stalkR.setAngles(right / 6 - 0.1309F, 0, right * 0.0837F);
		this.clawL.setAngles(right / 7 + (entity.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty() ? 0.2618F : -0.2618F), -0.2618F, right * -0.0837F);
		this.clawR.setAngles(left / 7 + 0.2618F, 0.2618F, left * 0.0837F);
	}
}