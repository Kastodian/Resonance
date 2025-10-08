package net.moss.resonance.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;

public class UniqueStructureDataSaver extends PersistentState {
public List<String> name = new ArrayList<String>();
public List<BlockPos> pos = new ArrayList<BlockPos>();
public boolean generated = false;

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, WrapperLookup wrapperLookup) {
		nbt.putBoolean("isGenerated", generated);
		for (int i = 0; i < name.size(); i++) {
			nbt.put(name.get(i), NbtHelper.fromBlockPos(pos.get(i)));
		}
		return nbt;
	}

	public static UniqueStructureDataSaver createFromNbt(NbtCompound tag) {
		UniqueStructureDataSaver state = new UniqueStructureDataSaver();
		state.generated = tag.getBoolean("isGenerated");
		for (String key : tag.getKeys()) {
			if (key.equals("isGenerated")) { continue; }
			state.name.add(key);
			Optional<BlockPos> block = NbtHelper.toBlockPos(tag, key);
			if (block.isPresent()) {
				state.pos.add(block.get());
			}
		}
		return state;
	}

	public static PersistentState.Type<UniqueStructureDataSaver> getPersistentStateType(ServerWorld world) {
		return new PersistentState.Type<UniqueStructureDataSaver>(UniqueStructureDataSaver::new, (nbt, registryLookup) -> UniqueStructureDataSaver.createFromNbt(nbt), null);
	}

	public static UniqueStructureDataSaver getServerState(MinecraftServer server) {
		PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
		UniqueStructureDataSaver state = persistentStateManager.getOrCreate(getPersistentStateType(server.getOverworld()), Resonance.MOD_ID);
		state.markDirty();

		return state;
	}
}