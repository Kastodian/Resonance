package net.moss.resonance.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.event.PlayerCritEvent;
import net.moss.resonance.event.ShieldEvents;
import net.moss.resonance.item.CustomShield;
import net.moss.resonance.util.EnchantUtils;
import net.moss.resonance.util.GroundPoundType;
import net.moss.resonance.util.Utils;
import net.moss.resonance.util.interfaces.PlayerEntityInterface;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerEntityInterface {
    @Unique private int critCount = 0;
    @Unique private int critTimer = 0;
    @Unique private boolean isGroundPounding = false;
    @Unique private GroundPoundType groundPoundType = null;

    @Inject(at = @At(value = "HEAD"), method = "damageShield(F)V", locals = LocalCapture.CAPTURE_FAILHARD)
    private void damageShield(float amount, CallbackInfo callBackInfo) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        Hand hand = player.getActiveHand();
        ItemStack activeItem = player.getActiveItem();
        ShieldEvents.SHIELD_BLOCK.invoker().block(player, amount, player.getActiveHand(), activeItem);
        if (activeItem.getItem() instanceof CustomShield) {
            if (amount >= 3.0F) {
                int i = 1 + MathHelper.floor(amount);
                activeItem.damage(i, (LivingEntity) player, PlayerEntity.getSlotForHand(player.getActiveHand()));
                if (activeItem.isEmpty()) {
                    if (hand == Hand.MAIN_HAND) { player.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY); }
                    else { player.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY); }
                    activeItem = ItemStack.EMPTY;
                    player.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + player.getRandom().nextFloat() * 0.4F);
                }
            }
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "disableShield()V", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void disableShieldHead(CallbackInfo callbackInfo) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack activeItemStack = player.getActiveItem();
        ShieldEvents.SHIELD_DISABLE.invoker().disable(player, player.getActiveHand(), activeItemStack);
        if (activeItemStack.getItem() instanceof CustomShield shield) {
            float f = 0.25F;
            if (player.isSprinting()) { f += 0.75F; }
            if (player.getRandom().nextFloat() < f) {
                player.getItemCooldownManager().set((Item) shield, shield.getCoolDownTicks());
                player.clearActiveItem();
                player.getWorld().sendEntityStatus(player, (byte) 30);
                callbackInfo.cancel();
            }
        }
    }

    @Inject(method = "createPlayerAttributes", at = @At("RETURN"))
    private static void addAttributes(final CallbackInfoReturnable<DefaultAttributeContainer.Builder> info) { info.getReturnValue().add(Resonance.JUMPS); }

    @Inject(at = @At(value = "HEAD"), method = "attack", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void attack(Entity target, CallbackInfo callbackInfo) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        World world = player.getWorld();
        if (world.isClient) { return; }
        EnchantUtils.recoilCheck(player, null, player.getMainHandStack(), 0.3f, 0.8f, false);
        if (Utils.isAttackCritical(player, target) && (critTimer > 0 || critCount == 0) && world instanceof ServerWorld serverWorld) {
            critCount += 1;
            critTimer = 50;
            EnchantUtils.onTargetCritDamaged(serverWorld, target, Utils.damageSource(serverWorld, DamageTypes.PLAYER_ATTACK, player), player.getMainHandStack());
            PlayerCritEvent.CRIT_EVENT.invoker().critStreak(player, target, critCount, player.getActiveHand(), player.getMainHandStack());
        } else {
            PlayerCritEvent.CRIT_DEATH_EVENT.invoker().critDeath(player, target, critCount, player.getActiveHand(), player.getMainHandStack());
            critCount = 0;
        }
    }

    @Inject(at = @At(value = "TAIL"), method = "tick", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void tick(CallbackInfo callbackInfo) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (critTimer == 1) { critCount = 0; }
        else if (critTimer > 0) { critTimer -= 1; }
        if (isGroundPounding && groundPoundType != null && player.getWorld() instanceof ServerWorld serverWorld) {
            if (player.isFallFlying() || player.isClimbing() || player.hasVehicle() || player.isTouchingWater() || player.getAbilities().flying) { ((PlayerEntityInterface)player).cancelGroundPound(player); }
            else { groundPoundType.tick(serverWorld, player); }
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "collideWithEntity", cancellable = true)
    private void collideWithEntity(Entity entity, CallbackInfo callbackInfo) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            if (isGroundPounding && groundPoundType != null) {
                ((PlayerEntityInterface)player).endGroundPound(serverWorld, player, entity);
            }
        }
    }

    @Override public int getCritCount() { return critCount; }
    @Override public int getCritTimer() { return critTimer; }
    @Override public void setCritCount(int value) { critCount = value; }
    @Override public void setCritTimer(int value) { critTimer = value; }
    @Override public boolean isGroundPounding() { return isGroundPounding; }
    @Override public GroundPoundType getGroundPoundType() { return groundPoundType; }

    @Override
    public void startGroundPound(ServerWorld world, PlayerEntity user, GroundPoundType type) {
        isGroundPounding = true;
        groundPoundType = type;
    }

    @Override
    public void endGroundPound(ServerWorld world, PlayerEntity user, @Nullable Entity target) {
        if (groundPoundType != null) { groundPoundType.end(world, user, target); }
        groundPoundType = null;
        isGroundPounding = false;
    }

    @Override
    public void cancelGroundPound(PlayerEntity user) {
        if (groundPoundType != null) { groundPoundType.cancel(user); }
        groundPoundType = null;
        isGroundPounding = false;
    }
}