package net.moss.resonance.item;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.item.ItemStack;

public class DyeableScarf extends ScarfItem {
    private static final List<String> SIMPLE_KEYS = Arrays.asList(
            ".*trans( | *gender).*",
            ".*( bi |bisexual).*",
            ".*( ace |asexual).*",
            ".*(lesbian|long *bacon).*",
            ".*gender *fluid.*",
            ".*( e*nby* |non *binary).*",
            ".*( aro |aromantic).*",
            ".*(gay|homo *sexual| *pride).*");

    public DyeableScarf(Settings settings, String modId, String texture) {
        super(settings, modId, texture);
    }

    @Override
    public int getSpecial(ItemStack stack) {
        String name = (" " + stack.getName().getString().toLowerCase() + " ").replaceAll("[._|=+^:;,'`¬¦&\\\"]|[|]", " ");
        Optional<String> first = SIMPLE_KEYS.stream().filter(name::matches).findFirst();
        return first.isPresent() ? SIMPLE_KEYS.indexOf(first.get()) + 1 : 0;
    }
}