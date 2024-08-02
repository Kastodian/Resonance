package net.moss.resonance.compat.trinkets;

import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketsApi;
import net.moss.resonance.Resonance;
import net.moss.resonance.item.ScarfTrinket;

public class TrinketsCompat {
    public static final Trinket PHANTOM_SCARF_TRINKET = new ScarfTrinket();
    public static final Trinket SPIRIT_SCARF_TRINKET = new ScarfTrinket();
    public static final Trinket ECHO_SCARF_TRINKET = new ScarfTrinket();

    public static void test() {
        TrinketsApi.registerTrinket(Resonance.PHANTOM_SCARF, PHANTOM_SCARF_TRINKET);
        TrinketsApi.registerTrinket(Resonance.SPIRIT_SCARF, SPIRIT_SCARF_TRINKET);
        TrinketsApi.registerTrinket(Resonance.ECHO_SCARF, ECHO_SCARF_TRINKET);
    }
}
