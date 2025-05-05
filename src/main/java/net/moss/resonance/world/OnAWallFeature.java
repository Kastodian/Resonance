package net.moss.resonance.world;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.state.property.Properties;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class OnAWallFeature extends Feature<OnAWallFeatureConfig> {

    public OnAWallFeature(Codec<OnAWallFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<OnAWallFeatureConfig> context) {
		StructureWorldAccess structureWorldAccess = context.getWorld();
		Random random = context.getRandom();
		OnAWallFeatureConfig onAWallFeatureConfig = context.getConfig();
        int desiredWallSize = onAWallFeatureConfig.getDesiredWallSize().get(random);
        int height = onAWallFeatureConfig.getHeight().get(random);
        int reach = onAWallFeatureConfig.getReach().get(random);
		BlockPos blockPos = context.getOrigin().add(0, height / 2, 0);
        BlockPredicate target = onAWallFeatureConfig.getTarget();
        BlockStateProvider wallState = onAWallFeatureConfig.getWallStateProvider();
        BlockStateProvider floorState = onAWallFeatureConfig.getFloorStateProvider();
        Boolean allignFacingToWall = onAWallFeatureConfig.getAllignFacingToWall();
        Map<BlockPos, Direction> wallPlacementsMap = new HashMap<>();
        List<Direction> directions = Arrays.asList(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
        Collections.shuffle(directions);
        BlockPos.iterateOutwards(blockPos, reach, height, reach).forEach(checkPos -> {
            BlockState checkState = structureWorldAccess.getBlockState(checkPos);
            if (!wallPlacementsMap.containsKey(checkPos) && !checkState.isReplaceable()) {
                BlockPos offsetPos = checkPos.down();
                if (target.test(structureWorldAccess, blockPos) && (Block.isFaceFullSquare(checkState.getSidesShape(structureWorldAccess, checkPos), Direction.UP) || Block.isFaceFullSquare(checkState.getCollisionShape(structureWorldAccess, checkPos), Direction.UP))) {
                    wallPlacementsMap.put(checkPos, Direction.DOWN);
                }
                if (wallPlacementsMap.containsKey(offsetPos)) { return; }
                for (Direction direction : directions) {
                    offsetPos = checkPos.offset(direction);
                    if (wallPlacementsMap.containsKey(offsetPos)) { continue; }
                    if (target.test(structureWorldAccess, blockPos)) {
                        if (structureWorldAccess.getBlockState(offsetPos.down()).isSideSolidFullSquare(structureWorldAccess, offsetPos.down(), Direction.UP)) {
                            wallPlacementsMap.put(offsetPos, Direction.DOWN);
                        } else if (Block.isFaceFullSquare(checkState.getSidesShape(structureWorldAccess, checkPos), direction.getOpposite()) || Block.isFaceFullSquare(checkState.getCollisionShape(structureWorldAccess, checkPos), direction.getOpposite())) {
                            wallPlacementsMap.put(offsetPos, direction.getOpposite());
                        }
                    }
                }
            }
        });
        BlockState state;
        if (wallPlacementsMap.size() > desiredWallSize) {
            for (Map.Entry<BlockPos, Direction> entry : wallPlacementsMap.entrySet()) {
                if (entry.getValue() == Direction.DOWN) {
                    state = floorState.get(random, entry.getKey());
                    if (!state.canPlaceAt(structureWorldAccess, entry.getKey())) { continue; }
                    structureWorldAccess.setBlockState(entry.getKey(), floorState.get(random, entry.getKey()), Block.NOTIFY_ALL);
                } else {
                    state = wallState.get(random, entry.getKey());
                    if (!state.canPlaceAt(structureWorldAccess, entry.getKey())) { continue; }
                    else if (state.contains(Properties.HORIZONTAL_FACING) && allignFacingToWall) {
                        structureWorldAccess.setBlockState(entry.getKey(), wallState.get(random, entry.getKey()).with(Properties.HORIZONTAL_FACING, entry.getValue()), Block.NOTIFY_ALL);
                    } else {
                        structureWorldAccess.setBlockState(entry.getKey(), wallState.get(random, entry.getKey()), Block.NOTIFY_ALL);
                    }
                }
            }
            return true;
        }
        return false;
    }
}
