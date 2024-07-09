package net.moss.resonance.mixin;

import net.minecraft.block.spawner.MobSpawnerLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MobSpawnerLogic.class)
public interface MobSpawnerLogicAccessor {
    @Accessor("spawnDelay")
    public void setSpawnDelay(int spawnDelay);
}
