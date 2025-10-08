package net.moss.resonance.util;

import java.util.List;
import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;

public enum PearlNatures implements StringIdentifiable {
	AGITATED("agitated", Ints.asList(2, 4, 3)),
	PERIODIC("periodic", Ints.asList(4, 2, 3)),
	BRILLIANT("brilliant", Ints.asList(2, 3, 5)),
	WHIMSICAL("whimsical", Ints.asList(3, 3, 4)),
	CALM("calm", Ints.asList(3, 3, 3));

	private final String name;
	private final List<Integer> stats;

	private PearlNatures(final String name, List<Integer> stats) {
		this.name = name;
		this.stats = stats;
	}

	public static final Codec<PearlNatures> CODEC = StringIdentifiable.createBasicCodec(PearlNatures::values);
	public Text getTranslationText() { return Text.translatable("pearl.nature." + this.name); }
	public List<Integer> getStats() { return stats; }
	public final int getDuration() { return stats.get(0); }
	public final int getRecuperation() { return stats.get(1); }
	public final int getStrength() { return stats.get(2); }
	public static PearlNatures random(Random random) { return Util.getRandom(PearlNatures.values(), random); }
	public static PearlNatures random(java.util.Random random) { return PearlNatures.values()[random.nextInt(5)]; }

	@Override public String asString() { return this.name; }
}