package net.moss.resonance.mixin;

import net.minecraft.block.AmethystClusterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.util.Dischargable;
import net.moss.resonance.util.Utils;
import net.moss.resonance.event.ReverberateEvent;
import net.moss.resonance.particle.ModParticles;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mixin(LightningEntity.class)
public class LightningEntityMixin {
	@Shadow private int ambientTick;
	@Shadow private BlockPos getAffectedBlockPos() { return null; };
	@Unique private final Set<BlockPos> conductedBlocks = Sets.newHashSet();

	@Inject(at = @At("HEAD"), method = "tick", cancellable = true)
	public void tick(CallbackInfo info) {
		if (this.ambientTick == 2) {
			BlockPos blockPos = this.getAffectedBlockPos();
			conductElectricity(blockPos, 12);
			for (BlockPos blockPos2 : conductedBlocks) { electrify(blockPos2); }
			LightningEntity Lightning = (LightningEntity)(Object) this;
			if (Lightning.getWorld() instanceof ServerWorld world) {
				List<Entity> list = Utils.entitiesInRadius(world, Lightning.getPos(), Lightning, 2, 2, Entity::isAlive);
	            for (Entity entity2 : list) {
            	    if (entity2 instanceof LivingEntity entity1) {
						for (ItemStack itemStack : Utils.getAllEquipped(entity1, true)) {
							if (itemStack.isIn(Resonance.CONDUCTIVE_ITEMS_TAG) || entity1.getSteppingBlockState().isIn(Resonance.CONDUCTIVE_BLOCKS_TAG)) {
								world.spawnParticles(ModParticles.SPARK_PARTICLE, entity2.getX(), entity2.getY() + entity2.getHeight()/2, entity2.getZ(), 20, 0, 0, 0, 1);
								entity1.takeKnockback(0.5, Lightning.getX() - entity1.getX(), Lightning.getZ() - entity1.getZ());
								entity1.addStatusEffect(new StatusEffectInstance(Resonance.SPARKING, 15 * 20, 2));
								break;
							}
						}
        	        }
            	}
			}
			
        }
	}

	public void conductElectricity(BlockPos blockPos, int strength) {
		LightningEntity Lightning = (LightningEntity)(Object) this;
        World world = Lightning.getWorld();
		--strength;
		if (world.getBlockState(blockPos).isIn(Resonance.CONDUCTIVE_BLOCKS_TAG)) {
			if (!conductedBlocks.contains(blockPos)) { conductedBlocks.add(blockPos); }
			if (strength >= 0) {
				BlockPos blockPos2 = blockPos.offset(Direction.random(Random.create()));
				for (int i = 0; i < 6; ++i) {
					if (!Lightning.getWorld().getBlockState(blockPos2).isIn(Resonance.CONDUCTIVE_BLOCKS_TAG) && !conductedBlocks.contains(blockPos2)) {
						blockPos2 = blockPos.offset(Direction.random(Random.create()));
					}
					else { conductElectricity(blockPos2, strength - 1); }
				}
			}
		}
	}

	public void electrify(BlockPos blockPos) {
		LightningEntity Lightning = (LightningEntity)(Object) this;
        World world = Lightning.getWorld();
		BlockState state = world.getBlockState(blockPos);
		if (state.getBlock() instanceof AmethystClusterBlock) {
			Optional<BlockState> blockstate = Dischargable.getIncreasedChargeState(state);
			if (blockstate.isPresent()) {
				world.setBlockState(blockPos, blockstate.get(), Block.NOTIFY_LISTENERS);
				Vector3f rotOffest = new Vector3f(0,0,0);
				Direction direction = state.get(AmethystClusterBlock.FACING);
				if (direction.getAxis().isVertical()) { rotOffest = new Vector3f(1.57f, 1.57f, 0); }
				else if (direction.getAxis() == Axis.X) { rotOffest = new Vector3f(0.1f, 1.57f, 0); }
				else { rotOffest = new Vector3f(0.1f, 1.57f, 0); }
				ReverberateEvent.REVERBERATE.invoker().onResonate(world, blockPos.toCenterPos(), rotOffest, null, Lightning, 2, false);
			}
		}
	}
}