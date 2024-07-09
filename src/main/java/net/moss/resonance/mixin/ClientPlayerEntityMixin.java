package net.moss.resonance.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.moss.resonance.Resonance;
import net.moss.resonance.networking.JumpPayload;
import net.moss.resonance.util.interfaces.ClientPlayerEntityInterface;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin implements ClientPlayerEntityInterface {
    @Unique private double jumpCount = 0;
    @Unique private boolean jumpedLastTick = false;
    @Unique private float crushingWeightIntensity;
    @Unique private float prevCrushingWeightIntensity;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void tickMovement(CallbackInfo info) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player.isOnGround() || player.isClimbing()) {
            jumpCount = player.getAttributeValue(Resonance.JUMPS);
        } else if (!jumpedLastTick && (jumpCount > 0 || player.hasStatusEffect(Resonance.NORMAL_DOUBLE_JUMP) || player.hasStatusEffect(Resonance.STRONG_DOUBLE_JUMP))) {
            if (player.input.jumping && !player.isFallFlying() && !player.hasVehicle() && !player.isTouchingWater() && !player.getAbilities().flying && player.getVelocity().y <= 0) {
                player.jump();
                if (!(player.hasStatusEffect(Resonance.NORMAL_DOUBLE_JUMP) || player.hasStatusEffect(Resonance.STRONG_DOUBLE_JUMP))) {
                    jumpCount--;
                }
                ClientPlayNetworking.send(new JumpPayload());
            }
        }
        jumpedLastTick = player.input.jumping;

        this.prevCrushingWeightIntensity = this.crushingWeightIntensity;
        float f = 0.0f;
        if (player.hasStatusEffect(Resonance.CRUSHING_WEIGHT)) {
            f = 0.006666667f * (player.getStatusEffect(Resonance.CRUSHING_WEIGHT).getAmplifier() + 1);
        } else if (this.crushingWeightIntensity > 0.0f) {
            f = -0.05f;
        }
        this.crushingWeightIntensity = MathHelper.clamp(this.crushingWeightIntensity + f, 0.0f, 1.0f);
    }
    
    @Override public float getCrushingWeightIntensity() { return crushingWeightIntensity; }
    @Override public float getPrevCrushingWeightIntensity() { return prevCrushingWeightIntensity; }
    @Override public void setCrushingWeightIntensity(float value) { crushingWeightIntensity = value; }
    @Override public void setPrevCrushingWeightIntensity(float value) { prevCrushingWeightIntensity = value; }
}