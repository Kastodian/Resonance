package net.moss.resonance.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.argument.RegistryPredicateArgumentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;
import net.moss.resonance.Resonance;
import net.moss.resonance.world.UniqueStructureDataSaver;
import net.moss.resonance.world.UniqueStructurePlacement;
import net.minecraft.text.ClickEvent.Action;

public class LocateUniqueStructureCommand {
	private static final DynamicCommandExceptionType STRUCTURE_INVALID_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable("commands.locate.structure.invalid", id));

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
		dispatcher.register(CommandManager.literal("unique").requires(source -> source.hasPermissionLevel(2))
			.then(CommandManager.literal("regenerate")
				.executes(context -> { return regenerate(context); }))	
			.then(CommandManager.literal("find")
				.then(CommandManager.argument("structure", RegistryPredicateArgumentType.registryPredicate(RegistryKeys.STRUCTURE))
					.executes(context -> { return find(context, RegistryPredicateArgumentType.getPredicate(context, "structure", RegistryKeys.STRUCTURE, STRUCTURE_INVALID_EXCEPTION)); }))
		));
	}

	private static int regenerate(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		UniqueStructureDataSaver serverState = UniqueStructureDataSaver.getServerState(source.getServer());
		source.getWorld().getChunkManager().getStructurePlacementCalculator().getStructureSets().forEach(structureSet -> {
			if (structureSet.value().placement() instanceof UniqueStructurePlacement placement) {
				String name = structureSet.getIdAsString();
				if (!serverState.name.contains(name)) { return; }
				BlockPos pos = serverState.pos.get(serverState.name.indexOf(name));
				placement.setChunk(pos.getX() >> 4, pos.getZ() >> 4);
				Resonance.LOGGER.info("Assigned [" + name + "] To: " + pos);
			}
		});
		source.sendFeedback(() -> { return Text.translatable("command.unique_structure.regenerate.success"); }, false);
		return 0;
	}

	private static int find(CommandContext<ServerCommandSource> context, RegistryPredicateArgumentType.RegistryPredicate<Structure> predicate) {
		String uniqueStructure = predicate.getKey().left().get().getValue().toString();
		System.out.println(uniqueStructure);
		ServerCommandSource source = context.getSource();
		UniqueStructureDataSaver uniqueStructureDataSaver = UniqueStructureDataSaver.getServerState(source.getServer());
		int i = uniqueStructureDataSaver.name.indexOf(uniqueStructure);
		if (i == -1) { 
			source.sendFeedback(() -> {
				return Text.translatable("command.unique_structure.unique_structure_not_found").formatted(Formatting.RED);
			}, false);
			return 0;
		}
		BlockPos blockPos = uniqueStructureDataSaver.pos.get(i);
		Text text = Texts.bracketed(Text.translatable("chat.coordinates", new Object[]{blockPos.getX(), "~", blockPos.getZ()})).styled((style) -> {
			return style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/tp @s " + blockPos.getX() + " " + "~" + " " + blockPos.getZ())).withHoverEvent(new HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.coordinates.tooltip")));
		});
		source.sendFeedback(() -> {
			return Text.translatable("command.unique_structure.find.success", new Object[]{uniqueStructure, text});
		}, false);
		return 0;
	}
}