package net.moss.resonance.event;

import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class ShieldEvents {
	private ShieldEvents() { }

    public static final Event<ShieldBlock> SHIELD_BLOCK = EventFactory.createArrayBacked(ShieldBlock.class,
            (listeners) -> (defender, amount, hand, shield) -> {
                for (ShieldBlock listener : listeners) {
                    ActionResult result = listener.block(defender, amount, hand, shield);
                    if (result != ActionResult.PASS) { return result; }
                }
                return ActionResult.PASS;
    });

    public static final Event<ShieldParry> SHIELD_PARRY = EventFactory.createArrayBacked(ShieldParry.class,
            (listeners) -> (defender, source, amount, hand, shield) -> {
                for (ShieldParry listener : listeners) {
                    ActionResult result = listener.parry(defender, source, amount, hand, shield);
                    if (result != ActionResult.PASS) { return result; }
                }
                return ActionResult.PASS;
    });

    public static final Event<ShieldDisable> SHIELD_DISABLE = EventFactory.createArrayBacked(ShieldDisable.class,
            (listeners) -> (defender, hand, shield) -> {
                for (ShieldDisable event : listeners) {
                    ActionResult result = event.disable(defender, hand, shield);
                    if (result != ActionResult.PASS) { return result; }
                }
                return ActionResult.PASS;
    });

    @FunctionalInterface public interface ShieldBlock { ActionResult block(LivingEntity defender, float amount, Hand hand, ItemStack shield); }
    @FunctionalInterface public interface ShieldParry { ActionResult parry(LivingEntity defender, @Nullable DamageSource source, float amount, Hand hand, ItemStack shield); }
	@FunctionalInterface public interface ShieldDisable { ActionResult disable(PlayerEntity defender, Hand hand, ItemStack shield); }
    
    public static void registerEvents() { }
}