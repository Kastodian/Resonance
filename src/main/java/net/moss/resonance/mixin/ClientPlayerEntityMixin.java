package net.moss.resonance.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.util.math.Direction.Axis;
import net.moss.resonance.Resonance;
import net.moss.resonance.networking.JumpPayload;
import net.moss.resonance.util.interfaces.ClientPlayerEntityInterface;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin implements ClientPlayerEntityInterface {
	@Unique private double doubleJumpCount = 0;
	@Unique private double wallJumpCount = 0;
	@Unique private boolean jumpedLastTick = false;
	@Unique private float crushingWeightIntensity;
	@Unique private float prevCrushingWeightIntensity;
	@Shadow private boolean wouldCollideAt(BlockPos pos) { return false; };

	@Inject(method = "tickMovement", at = @At("HEAD"))
	private void tickMovement(CallbackInfo info) {
		ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
		if (player.isOnGround() || player.isClimbing()) {
			doubleJumpCount = player.getAttributeValue(Resonance.DOUBLE_JUMPS);
			wallJumpCount = player.getAttributeValue(Resonance.WALL_JUMPS);
		} else if (player.input.jumping && !player.isFallFlying() && !player.hasVehicle() && !player.isTouchingWater() && !player.getAbilities().flying && player.getVelocity().y <= 0) {
			boolean wallJumped = false;
			if (wallJumpCount > 0) {
				Axis upAxis = Direction.UP.getAxis();
				World world = player.getWorld();
				Vec3d velocity = new Vec3d(0, 0, 0);
				Direction forwards = player.getHorizontalFacing();
				Box box = player.getBoundingBox();
				box = new Box(box.getMinPos(), new Vec3d(box.maxX, box.minY + player.getHeight() / 2, box.maxZ));
				Direction[] directions = {forwards, forwards.rotateClockwise(upAxis), forwards.getOpposite(), forwards.rotateCounterclockwise(upAxis)};
				boolean[] inputs = {player.input.pressingForward, player.input.pressingRight, player.input.pressingBack, player.input.pressingLeft};
				for (int i = 0; i < 4; i++) {
					if (!inputs[i]) { continue; }
					Vec3d vec = new Vec3d(directions[i].getOffsetX(), directions[i].getOffsetY(), directions[i].getOffsetZ()).multiply(0.01);
					if (world.isSpaceEmpty(box.stretch(vec))) { continue; }
					velocity = velocity.add(vec.negate());
				}
				if (velocity.length() > 0) {
					player.addVelocity(velocity.normalize().multiply(player.getMovementSpeed() * 2));
					player.jump();
					wallJumpCount--;
					wallJumped = true;
				}
			}
			if (!jumpedLastTick && !wallJumped && (doubleJumpCount > 0 || player.hasStatusEffect(Resonance.NORMAL_DOUBLE_JUMP) || player.hasStatusEffect(Resonance.STRONG_DOUBLE_JUMP))) {
				player.jump();
				if (!(player.hasStatusEffect(Resonance.NORMAL_DOUBLE_JUMP) || player.hasStatusEffect(Resonance.STRONG_DOUBLE_JUMP))) {
					doubleJumpCount--;
				}
				ClientPlayNetworking.send(new JumpPayload());
			}
		}
		jumpedLastTick = player.input.jumping;

		this.prevCrushingWeightIntensity = this.crushingWeightIntensity;
		float f = 0;
		float air = (float) (player.getAttributeValue(EntityAttributes.GENERIC_OXYGEN_BONUS) + (player.hasStatusEffect(StatusEffects.WATER_BREATHING) || player.hasStatusEffect(StatusEffects.CONDUIT_POWER) ? 3 : 1));
		if (player.hasStatusEffect(Resonance.CRUSHING_WEIGHT)) { f = 0.006666667f * (player.getStatusEffect(Resonance.CRUSHING_WEIGHT).getAmplifier() + 1) / air; }
		else if (this.crushingWeightIntensity > 0.0f) { f = -0.025f * air; }
		this.crushingWeightIntensity = MathHelper.clamp(this.crushingWeightIntensity + f, 0.0f, 1.0f);
	}

	@Override public float getCrushingWeightIntensity() { return crushingWeightIntensity; }
	@Override public float getPrevCrushingWeightIntensity() { return prevCrushingWeightIntensity; }
	@Override public void setCrushingWeightIntensity(float value) { crushingWeightIntensity = value; }
	@Override public void setPrevCrushingWeightIntensity(float value) { prevCrushingWeightIntensity = value; }
}