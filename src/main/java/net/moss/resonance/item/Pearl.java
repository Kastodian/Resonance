package net.moss.resonance.item;

import java.util.List;
import net.minecraft.item.tooltip.TooltipType;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.moss.resonance.Resonance;

public class Pearl extends Item {
    public Pearl(Settings settings) {
        super(settings.maxCount(16).rarity(Rarity.UNCOMMON));
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            this.RegisterModelPredicate();
        }
    }

    private void RegisterModelPredicate() {
        ModelPredicateProviderRegistry.register(Identifier.of("shine"), (itemStack, clientWorld, livingEntity, i) -> {
            return itemStack.contains(Resonance.PEARL_STATS) && itemStack.get(Resonance.PEARL_STATS).size() == 4 ? itemStack.get(Resonance.PEARL_STATS).get(3) : 0.0F;
        });
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        boolean bl = stack.contains(Resonance.PEARL_STATS);
        int duration = 3;
        if (stack.get(Resonance.PEARL_STATS).size() < 4) { return; }
        if (bl) { duration = stack.get(Resonance.PEARL_STATS).get(2); }
        if (stack.contains(DataComponentTypes.POTION_CONTENTS)) {
            stack.get(DataComponentTypes.POTION_CONTENTS).buildTooltip(tooltip::add, 1.0f / (4 - duration), context.getUpdateTickRate());
        }
        if (type.isAdvanced() && bl) {
            List<Integer> pealStats = stack.get(Resonance.PEARL_STATS);
            tooltip.add(Text.literal(pealStats.get(0) + " " + pealStats.get(1) + " " + pealStats.get(2) + " " + pealStats.get(3)).formatted(Formatting.DARK_GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }
}