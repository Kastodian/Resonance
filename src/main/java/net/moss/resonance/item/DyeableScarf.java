package net.moss.resonance.item;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.item.ItemStack;

public class DyeableScarf extends ScarfItem {
    private static final List<String> KEYS = Arrays.asList(
            ".*trans( | *gender).*",
            ".* bi( | *sexual).*",
            ".*( ace |asexual).*",
            ".*(lesb(ian|os* )|long *bacon|(girl|wom(e|a)n) *kisser| w *l *w ).*",
            ".*gender *fluid.*",
            ".*( e*nby* |non *binary).*",
            ".*aro( *mantic)*.*",
            ".* inter *sex.*",
            ".*((boy|m(e|a)n) *kisser| m *l *m ).*",
            ".*pan( *sexual)*.*",
            ".* poly( | *mourous).*",
            ".*demi *sexual.*",
            ".*(aro *ace|sonic *(and|n)* *tails).*",
            ".*(gay|homo *sexual| *pride).*");

    public DyeableScarf(Settings settings, String modId, String texture) {
        super(settings, modId, texture);
    }

    @Override
    public int getSpecial(ItemStack stack) {
        String name = " " + stack.getName().getString().toLowerCase().replaceAll("[._|=+^:;,'`¬¦&\\\"]|[|]", " ") + " ";
        Optional<String> first = KEYS.stream().filter(name::matches).findFirst();
        return first.isPresent() ? KEYS.indexOf(first.get()) + 1 : 0;
    }
}