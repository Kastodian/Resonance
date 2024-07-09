package net.moss.resonance.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class PlayerCritEvent {
	private PlayerCritEvent() { }

    public static final Event<PlayerCritStreak> CRIT_EVENT = EventFactory.createArrayBacked(PlayerCritStreak.class,
            (listeners) -> (attacker, victim, critCount, hand, weapon) -> {
                for (PlayerCritStreak listener : listeners) {
                    ActionResult result = listener.critStreak(attacker, victim, critCount, hand, weapon);
                    if (result != ActionResult.PASS) { return result; }
                }
                return ActionResult.PASS;
    });

    public static final Event<PlayerCritDeath> CRIT_DEATH_EVENT = EventFactory.createArrayBacked(PlayerCritDeath.class,
            (listeners) -> (attacker, victim, critCount, hand, weapon) -> {
                for (PlayerCritDeath listener : listeners) {
                    ActionResult result = listener.critDeath(attacker, victim, critCount, hand, weapon);
                    if (result != ActionResult.PASS) { return result; }
                }
                return ActionResult.PASS;
    });

    @FunctionalInterface public interface PlayerCritStreak { ActionResult critStreak(PlayerEntity attacker, Entity target, int critCount, Hand hand, ItemStack weapon); }
    @FunctionalInterface public interface PlayerCritDeath { ActionResult critDeath(PlayerEntity attacker, Entity target, int critCount, Hand hand, ItemStack weapon); }
    
    public static void registerEvents() { }
}