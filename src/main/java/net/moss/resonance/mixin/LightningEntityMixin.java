package net.moss.resonance.mixin;

import net.minecraft.block.AmethystClusterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.util.Utils;
import net.moss.resonance.block.ChargedAmethystBlock;
import net.moss.resonance.event.ReverberateEvent;
import net.moss.resonance.particle.ModParticles;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;

@Mixin(LightningEntity.class)
public class LightningEntityMixin {
	@Shadow private int ambientTick;
	@Shadow private BlockPos getAffectedBlockPos() { return null; };
	private final Set<BlockPos> conductedBlocks = Sets.newHashSet();

	@Inject(at = @At("HEAD"), method = "tick", cancellable = true)
	public void tick(CallbackInfo info) {
		if (this.ambientTick == 2) {
			BlockPos blockPos = this.getAffectedBlockPos();
			conductElectricity(blockPos, 12);
			for (BlockPos blockPos2 : conductedBlocks) {
				electrify(blockPos2);
			}
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
			if (!conductedBlocks.contains(blockPos)) {
				conductedBlocks.add(blockPos);
			}
			if (strength >= 0) {
				BlockPos blockPos2 = blockPos.offset(Direction.random(Random.create()));
				for (int i = 0; i < 6; ++i) {
					if (!Lightning.getWorld().getBlockState(blockPos2).isIn(Resonance.CONDUCTIVE_BLOCKS_TAG) && !conductedBlocks.contains(blockPos2)) {
						blockPos2 = blockPos.offset(Direction.random(Random.create()));
					}
					else {
						conductElectricity(blockPos2, strength - 1);
					}
				}
			}
		}
	}

	public void electrify(BlockPos blockPos) {
		LightningEntity Lightning = (LightningEntity)(Object) this;
        World world = Lightning.getWorld();
		Block block = world.getBlockState(blockPos).getBlock();
		BlockState state = world.getBlockState(blockPos);
		if (block == Blocks.AMETHYST_CLUSTER || block == Blocks.LARGE_AMETHYST_BUD || block == Blocks.MEDIUM_AMETHYST_BUD || block == Blocks.SMALL_AMETHYST_BUD || block == Resonance.CHARGED_AMETHYST_CLUSTER) {
			Direction direction = state.get(ChargedAmethystBlock.FACING);
			if (block != Resonance.CHARGED_AMETHYST_CLUSTER) {
				direction = state.get(AmethystClusterBlock.FACING);
				int stage = 0;
				if (block == Blocks.MEDIUM_AMETHYST_BUD) {stage = 1;}
				else if (block == Blocks.SMALL_AMETHYST_BUD) {stage = 2;}
				BlockState blockState = (BlockState)Resonance.CHARGED_AMETHYST_CLUSTER.getDefaultState().with(ChargedAmethystBlock.FACING, state.get(AmethystClusterBlock.FACING)).with(ChargedAmethystBlock.WATERLOGGED, state.get(AmethystClusterBlock.WATERLOGGED)).with(ChargedAmethystBlock.STAGE, stage);
				world.setBlockState(blockPos, blockState, Block.NOTIFY_LISTENERS);
			}
			else if (state.get(ChargedAmethystBlock.STAGE) >=1) {
				BlockState blockState = (BlockState)Resonance.CHARGED_AMETHYST_CLUSTER.getDefaultState().with(ChargedAmethystBlock.FACING, state.get(ChargedAmethystBlock.FACING)).with(ChargedAmethystBlock.WATERLOGGED, state.get(ChargedAmethystBlock.WATERLOGGED)).with(ChargedAmethystBlock.STAGE, state.get(ChargedAmethystBlock.STAGE) - 1);
				world.setBlockState(blockPos, blockState, Block.NOTIFY_LISTENERS);
			}
			Vec3d posOffest = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
			Vector3f rotOffest = new Vector3f(0,0,0);
			if (direction == Direction.DOWN) {
				posOffest = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.5, 0.8, 0.5);
				rotOffest = new Vector3f(1.57f, 1.57f, 0);
			} else if (direction == Direction.UP) {
				posOffest = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.5, 0.2, 0.5);
				rotOffest = new Vector3f(1.57f, 1.57f, 0);
			} else if (direction == Direction.NORTH) {
				posOffest = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.5, 0.5, 0.8);
			} else if (direction == Direction.SOUTH) {
				posOffest = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.5, 0.5, 0.2);
			} else if (direction == Direction.EAST) {
				posOffest = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.2, 0.5, 0.5);
				rotOffest = new Vector3f(0.1f, 1.57f, 0);
			} else {
				posOffest = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.8, 0.5, 0.5);
				rotOffest = new Vector3f(0.1f, 1.57f, 0);
			}
			ReverberateEvent.REVERBERATE.invoker().onResonate(world, posOffest, rotOffest, null, Lightning, 2, false);
		}
	}
}