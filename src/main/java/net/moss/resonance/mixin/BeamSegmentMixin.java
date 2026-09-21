package net.moss.resonance.mixin;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.moss.resonance.util.interfaces.BeamSegmentInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BeaconBlockEntity.BeamSegment.class)
public class BeamSegmentMixin implements BeamSegmentInterface {
	@Unique private boolean hidden = false;
	@Override public boolean isHidden() { return this.hidden; }
	@Override public void setHidden(boolean hidden) { this.hidden = hidden; }
}