package net.moss.resonance.event;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class ReverberateEvent {
	private ReverberateEvent() { }

	public static final Event<Reverberate> REVERBERATE = EventFactory.createArrayBacked(Reverberate.class,
			(listeners) -> (world, pos, angle, immuneEntity, sourceEntity, strength, windCharged) -> {
				for (Reverberate event : listeners) { event.onResonate(world, pos, angle, immuneEntity, sourceEntity, strength, windCharged); }
			}
	);

	@FunctionalInterface public interface Reverberate { void onResonate(World world, Vec3d pos, Vector3f angle, @Nullable Entity immuneEntity, @Nullable Entity sourceEntity, float strength, boolean windCharged); }

    public static void registerEvents() { }
}