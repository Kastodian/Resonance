package net.moss.resonance.item;

import net.minecraft.item.ItemStack;

public class DyeableScarf extends ScarfItem {
    public DyeableScarf(Settings settings, String modId, String texture) {
        super(settings, modId, texture);
    }

    @Override
    public int getSpecial(ItemStack stack) {
        String name = stack.getName().getString().toLowerCase();
        if (name.equals("trans") || name.equals("trans pride") || name.equals("trans pride scarf") || name.equals("transgender") || name.equals("transgender pride") || name.equals("transgender pride scarf")) {
            return 1;
        } else if (name.equals("bi") || name.equals("bi pride") || name.equals("bi pride scarf") || name.equals("bisexual") || name.equals("bisexual pride") || name.equals("bisexual pride scarf")) {
            return 2;
        } else if (name.equals("gay") || name.equals("gay pride") || name.equals("gay pride scarf")) {
            return 3;
        } else if (name.equals("ace") || name.equals("ace pride") || name.equals("ace pride scarf") || name.equals("asexual") || name.equals("asexual pride") || name.equals("asexual pride scarf")) {
            return 4;
        } else if (name.equals("lesbian") || name.equals("lesbian pride") || name.equals("lesbian pride scarf") || name.equals("long bacon")) {
            return 5;
        }
        return 0;
    }
}