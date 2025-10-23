package net.moss.resonance.mixin;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BeaconBlockEntity.BeamSegment;
import net.minecraft.world.World;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BeaconBlockEntity.class)
public interface BeaconBlockEntityAccessor {
	@Accessor("field_19178") public void setField_19178(List<BeamSegment> field_19178);
	@Accessor("beamSegments") public void setBeamSegments(List<BeamSegment> beamSegments);
	@Accessor("minY") public void setMinY(int minY);
	@Invoker("updateLevel") public static int updateLevel(World world, int x, int y, int z) { throw new AssertionError(); }
}