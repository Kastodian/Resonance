package net.moss.resonance.mixin;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.util.EnchantUtils;
import net.moss.resonance.util.Utils;
import net.moss.resonance.util.interfaces.PlayerEntityInterface;
import net.moss.resonance.entity.LostSoulEntity;
import net.moss.resonance.entity.ModEntities;
import net.moss.resonance.event.ReverberateEvent;
import net.moss.resonance.event.ShieldEvents;
import net.moss.resonance.event.StaticShockEvent;
import net.moss.resonance.item.CustomShield;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    private boolean echoScarfDamageHalf = false;

    @Inject(at = @At(value = "HEAD"), method = "getPreferredEquipmentSlot", cancellable = true)
    private void getPreferredEquipmentSlot(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> callbackinfo) {
        if(stack.getItem() instanceof CustomShield) {
            callbackinfo.setReturnValue(EquipmentSlot.OFFHAND);
        }
    }

    @ModifyConstant(method = "isBlocking", constant = @Constant(intValue = 5))
    private int isBlocking(int value) {
        return 0;
    }

    @Inject(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageShield(F)V", shift = At.Shift.AFTER))
    private void parry(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ret) {
        LivingEntity defender = ((LivingEntity)(Object)this);
        Hand hand = defender.getActiveHand();
        ItemStack activeItem = defender.getActiveItem();
        if (defender.getItemUseTimeLeft() >= activeItem.getItem().getMaxUseTime(activeItem, defender) - 5) {
            ShieldEvents.SHIELD_PARRY.invoker().parry(defender, source, amount, hand, activeItem);
        }
    }

	@Inject(at = @At("HEAD"), method = "damage")
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ret) {
        LivingEntity entity = (LivingEntity)(Object) this;
        Random random = entity.getRandom();
        World world = entity.getWorld();
        LivingEntity attacker = (LivingEntity) null;
        boolean getAttacker = true;
        List<ItemStack> allEquippedHands = Utils.getAllEquipped(entity, true);
        if (source.getAttacker() instanceof LivingEntity livingEntity) { attacker = livingEntity; }
        else if (source.getAttacker() instanceof ProjectileEntity projectileEntity && projectileEntity.getOwner() instanceof LivingEntity ownerEntity) { attacker = ownerEntity; } 
        else { getAttacker = false; }

        if (getAttacker) { for (ItemStack itemStack : Utils.getAllEquipped(entity, false)) { EnchantUtils.recoilCheck(entity, null, itemStack, 0.3f, 0.8f, false); } }
        if (source.isOf(DamageTypes.FALLING_ANVIL)) { entity.addStatusEffect(new StatusEffectInstance(Resonance.CRUSHING_WEIGHT, 30, 2)); }

        if (world instanceof ServerWorld serverWorld && entity.hasStatusEffect(StatusEffects.WIND_CHARGED)) {
            if (world.random.nextInt(1 + entity.getStatusEffect(StatusEffects.WIND_CHARGED).getAmplifier()) != 0) {
                double d = entity.getX();
                double e = entity.getY() + (double)(entity.getHeight() / 2.0f);
                double f = entity.getZ();
                float g = 3.0f + random.nextFloat() * 2.0f;
                serverWorld.createExplosion(entity, null, AbstractWindChargeEntity.EXPLOSION_BEHAVIOR, d, e, f, g, false, World.ExplosionSourceType.TRIGGER, ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE, SoundEvents.ENTITY_BREEZE_WIND_BURST);
            }
        }
        
        if (entity.hasStatusEffect(Resonance.SPARKING) && !source.isOf(Resonance.SPARKING_DAMAGE)) {
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), Resonance.ELECTRIC_SPARK_SOUND.value(), SoundCategory.BLOCKS, 0.1f, 1f);
            for (ItemStack itemStack : allEquippedHands) {
                if (itemStack.isIn(Resonance.CONDUCTIVE_ITEMS_TAG) || entity.getSteppingBlockState().isIn(Resonance.CONDUCTIVE_BLOCKS_TAG) || entity.getBlockStateAtPos().isIn(Resonance.CONDUCTIVE_BLOCKS_TAG) || entity.getActiveItem().isIn(Resonance.CONDUCTIVE_ITEMS_TAG)) {
                    int amplifier = entity.getStatusEffect(Resonance.SPARKING).getAmplifier();
                    StaticShockEvent.STATIC_SHOCK.invoker().staticShock(entity, amplifier, ((LivingEntity)attacker));
                    break;
                }
            }
            if (getAttacker) {
                for (ItemStack itemStack : attacker.getHandItems()) {
                    if (itemStack.isIn(Resonance.CONDUCTIVE_ITEMS_TAG)) {
                        int amplifier = entity.getStatusEffect(Resonance.SPARKING).getAmplifier();
                        StaticShockEvent.STATIC_SHOCK.invoker().staticShock(entity, amplifier, attacker);
                        break;
                    }
                }
            }
        }
    }

    @ModifyVariable(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInvulnerableTo(Lnet/minecraft/entity/damage/DamageSource;)Z"), argsOnly = true)
    private DamageSource modifyDamageSource(DamageSource source) {
        echoScarfDamageHalf = source.isOf(Resonance.REVERBERATION_DAMAGE) || (source.getAttacker() != null ? source.getAttacker().getType().isIn(Resonance.SOUND_SENSITIVE_TAG) : false);
        return source;
    }

    @ModifyVariable(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInvulnerableTo(Lnet/minecraft/entity/damage/DamageSource;)Z"), argsOnly = true)
    private float modifyDamage(float amount) {
        LivingEntity entity = (LivingEntity)(Object) this;
        if (Utils.isItemEquipped(entity, Resonance.ECHO_SCARF, false)) {
            ReverberateEvent.REVERBERATE.invoker().onResonate(entity.getWorld(), entity.getEyePos(), new Vector3f(0f, (float) (entity.getHeadYaw() / 180 * Math.PI), 0f), entity, entity, 1.5f, false);
            if (echoScarfDamageHalf) { return amount /= 1.5; }
        }
        if (Utils.isItemEquipped(entity, Resonance.SPIRIT_SCARF, false)) {
            float health = entity.getHealth();
            return health - amount > 1 || amount > health * 5 ? amount : (float) Math.clamp(1, (int) health - 1, (int) amount);
        }
        return amount;
    }

    @Inject(at = @At("HEAD"), method = "fall", cancellable = true)
    private void staticShockFall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition, CallbackInfo ret) {
        LivingEntity entity = (LivingEntity)(Object) this;
        World world = entity.getWorld();
        if (!world.isClient && onGround && entity.fallDistance > 0.0f && entity.hasStatusEffect(Resonance.SPARKING)) {
            for (ItemStack itemStack : Utils.getAllEquipped(entity, true)) {
                if (itemStack.isIn(Resonance.CONDUCTIVE_ITEMS_TAG)) {
                    int amplifier = entity.getStatusEffect(Resonance.SPARKING).getAmplifier();
                    StaticShockEvent.STATIC_SHOCK.invoker().staticShock(entity, amplifier, null);
                    return;
                }
            }
        }
        if (entity instanceof PlayerEntity player && player.isOnGround() && world instanceof ServerWorld serverWorld) {
            ((PlayerEntityInterface)player).endGroundPound(serverWorld, player, null);
        }
            
    }

    @Inject(at = @At(value = "HEAD"), method = "canFreeze", cancellable = true)
    private void canFreeze(CallbackInfoReturnable<Boolean> cir) {
        if (Utils.isItemEquipped((LivingEntity)(Object) this, Resonance.SCARF, false)){
            cir.setReturnValue(false);
        }
    }

	@Inject(at = @At(value = "HEAD"), method = "getArmorVisibility", cancellable = true)
    private void getArmorVisibility(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity)(Object) this;
        if (Utils.isItemEquipped(entity, Resonance.PHANTOM_SCARF, false) && entity.hasStatusEffect(StatusEffects.INVISIBILITY)) {
            cir.setReturnValue(0.0F);
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "getJumpBoostVelocityModifier", cancellable = true)
    protected void getJumpBoostVelocityModifier(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity)(Object) this;
        if (entity.hasStatusEffect(Resonance.STRONG_DOUBLE_JUMP)) { cir.setReturnValue(0.3F); }
        else if (entity.hasStatusEffect(Resonance.NORMAL_DOUBLE_JUMP)) { cir.setReturnValue(0.1F); }
    }

    @Inject(at = @At(value = "HEAD"), method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z", cancellable = true)
    private void addStatusEffect(StatusEffectInstance effect, @Nullable Entity source, CallbackInfoReturnable<Void> ret) {
        if (source instanceof PlayerEntity player) {
            Resonance.INFLICT_POTION_EFFECT_CRITERION.trigger((ServerPlayerEntity) player, (LivingEntity)(Object) this, effect);
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "onDeath", cancellable = true)
    protected void onDeath(DamageSource damageSource, CallbackInfo info) {
        LivingEntity entity = (LivingEntity)(Object) this;
        if (entity.getType().isIn(EntityTypeTags.SKELETONS) && (entity.hasStatusEffect(Resonance.SOUL_FATIGUE) || entity instanceof WitherSkeletonEntity)) {
            World world = entity.getWorld();
            if (entity.getRandom().nextInt(5) - world.getDifficulty().getId() <= 0) {
                LostSoulEntity lostSoulEntity = new LostSoulEntity(ModEntities.LOST_SOUL, world);
                lostSoulEntity.setPosition(entity.getEyePos());
                lostSoulEntity.setVelocity(0, 0.1, 0);
                world.spawnEntity(lostSoulEntity);
            }
        }
    }

    // @Inject(method = "dropInventory", at = @At("TAIL"))
    // protected void dropInventory(CallbackInfo info) {
    //     PlayerEntity player = (PlayerEntity)(Object) this;
    //     if (player.getWorld().isClient()) { return; }
    //     if (StandHandler.canUseStand(player)) {
    //         Stand stand = StandHandler.getStand(player);
    //         stand.desummonStand(player);
    //         if (player.getWorld().getGameRules().getBoolean(ResonanceStands.EJECT_DISC_ON_DEATH)) {
    //             stand.ejectDisc(player);
    //         }
    //     }
    // }
}