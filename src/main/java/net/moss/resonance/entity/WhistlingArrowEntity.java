package net.moss.resonance.entity;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import net.minecraft.block.Blocks;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.event.ReverberateEvent;

public class WhistlingArrowEntity extends PersistentProjectileEntity {
    public WhistlingArrowEntity(EntityType<? extends WhistlingArrowEntity> entityType, World world) {
        super((EntityType<? extends PersistentProjectileEntity>)entityType, world);
    }

    public WhistlingArrowEntity(World world, LivingEntity owner, ItemStack stack, @Nullable ItemStack shotFrom) {
        super(ModEntities.WHISTLING_ARROW, owner, world, stack, shotFrom);
    }

    public WhistlingArrowEntity(World world, double x, double y, double z, ItemStack stack, @Nullable ItemStack shotFrom) {
        super(ModEntities.WHISTLING_ARROW, x, y, z, world, stack, shotFrom);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.inGround) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.NEUTRAL, 0.2f, 1f);
        }
    }

    @Override
    protected void onHit(LivingEntity target) {
        super.onHit(target);
        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.NEUTRAL, 1f, 1f);
        ReverberateEvent.REVERBERATE.invoker().onResonate(getWorld(), this.getPos(), new Vector3f((float) (this.prevPitch / 180 * Math.PI), (float) (this.prevYaw / 180 * Math.PI), 0f), this, this.getOwner(), 1f, false); 
    }

    @Override
    protected void onDeflected(Entity deflector, boolean fromAttack) {
        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.NEUTRAL, 1f, 1f);
        ReverberateEvent.REVERBERATE.invoker().onResonate(getWorld(), this.getPos(), new Vector3f((float) (this.prevPitch / 180 * Math.PI), (float) (this.prevYaw / 180 * Math.PI), 0f), this, this.getOwner(), 1f, false); 
        super.onDeflected(deflector, fromAttack);
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (!this.inGround) { return; }
        World world = getWorld();
        world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.NEUTRAL, 1f, 1f);
        BlockPos blockPos = blockHitResult.getBlockPos();
        if (world.getBlockState(blockPos).getBlock() == Blocks.TNT) {
            TntBlock.primeTnt(world, blockPos);
            world.removeBlock(blockPos, false);
        }
        ReverberateEvent.REVERBERATE.invoker().onResonate(world, this.getPos(), new Vector3f((float) (this.prevPitch / 180 * Math.PI), (float) (this.prevYaw / 180 * Math.PI), 0f), this, this.getOwner(), 1f, false); 
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return Resonance.WHISTLING_ARROW.getDefaultStack();
    }
}

