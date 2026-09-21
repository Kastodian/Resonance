package net.moss.resonance.util;

import java.util.List;
import java.util.function.Consumer;
import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.random.Random;

public record PearlStatsComponent(List<Integer> stats, boolean shiny, PearlNatures nature) implements TooltipAppender {
	public static final PearlStatsComponent DEFAULT = new PearlStatsComponent(Ints.asList(1, 1, 1), false, PearlNatures.CALM);
	public static final Codec<PearlStatsComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codecs.NONNEGATIVE_INT.listOf().fieldOf("stats").forGetter(PearlStatsComponent::stats),
			Codec.BOOL.optionalFieldOf("shiny", false).forGetter(PearlStatsComponent::shiny),
			PearlNatures.CODEC.optionalFieldOf("nature", PearlNatures.CALM).forGetter(PearlStatsComponent::nature)
	).apply(instance, PearlStatsComponent::new));

	@Override
	public void appendTooltip(TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
			MutableText text = Text.empty();
			if (type.isAdvanced()) {
				for (Integer integer : this.stats()) { text.append(integer + " "); }
				text.append(String.valueOf(this.shiny()) + " ");
			}
			text.append(this.nature().getTranslationText());
			tooltip.accept(text.formatted(Formatting.DARK_GRAY));
	}

	public final int getDuration() { return stats.get(0); }
	public final int getRecuperation() { return stats.get(1); }
	public final int getStrength() { return stats.get(2); }

	public static PearlStatsComponent randomStats(Random random, int shinyBound) { return randomStats(random, shinyBound, PearlNatures.random(random)); }
	public static PearlStatsComponent randomStats(Random random, int shinyBound, PearlNatures nature) { return new PearlStatsComponent(Ints.asList(random.nextInt(nature.getDuration()) + 1, random.nextInt(nature.getRecuperation()) + 1, random.nextInt(nature.getStrength()) + 1), random.nextInt(shinyBound) == 0, nature); }
	public static PearlStatsComponent randomStats(Random random, int duration, int recuperation, int strength, int shinyBound, PearlNatures nature) { return new PearlStatsComponent(Ints.asList(Math.min(duration + (random.nextInt(3) == 0 ? 1 : 0), nature.getDuration()), Math.min(recuperation + (random.nextInt(3) == 0 ? 1 : 0), nature.getRecuperation()), Math.min(strength + (random.nextInt(3) == 0 ? 1 : 0), nature.getStrength())), random.nextInt(shinyBound) == 0, nature); }
	public static PearlStatsComponent randomStats(Random random, int duration, int recuperation, int strength, int shinyBound) { return randomStats(random, duration, recuperation, strength, shinyBound, PearlNatures.random(random)); }
	public static PearlStatsComponent randomStats(java.util.Random random, int shinyBound) { return randomStats(random, shinyBound, PearlNatures.random(random)); }
	public static PearlStatsComponent randomStats(java.util.Random random, int shinyBound, PearlNatures nature) { return new PearlStatsComponent(Ints.asList(random.nextInt(nature.getDuration()) + 1, random.nextInt(nature.getRecuperation()) + 1, random.nextInt(nature.getStrength()) + 1), random.nextInt(shinyBound) == 0, nature); }
}