package net.moss.resonance.event;

import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;

public class StaticShockEvent {
	private StaticShockEvent() { }

    public static final Event<StaitcShock> STATIC_SHOCK = EventFactory.createArrayBacked(StaitcShock.class,
            (listeners) -> (source, amplifier, attacker) -> {
                for (StaitcShock event : listeners) { event.staticShock(source, amplifier, attacker); }
            }
    );

    @FunctionalInterface public interface StaitcShock { void staticShock(LivingEntity source, int amplifier, @Nullable LivingEntity attacker); }
    
    public static void registerEvents() { }
}