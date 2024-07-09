package net.moss.resonance.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

public class DatapackScarf extends NecklaceTrinket {

    public DatapackScarf(Settings settings, String modId, String texture) {
        super(settings, modId, texture);
    }

    @Override
    public int getSpecial(ItemStack stack) {
        if (stack.contains(DataComponentTypes.CUSTOM_MODEL_DATA)) {
            return stack.get(DataComponentTypes.CUSTOM_MODEL_DATA).value();
        }
        return 0;
    }
}