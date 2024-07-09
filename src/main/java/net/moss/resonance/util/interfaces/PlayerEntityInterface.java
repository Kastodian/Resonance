package net.moss.resonance.util.interfaces;

import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.moss.resonance.util.GroundPoundType;

public interface PlayerEntityInterface {
    int getCritCount();
    int getCritTimer();
    void setCritCount(int value);
    void setCritTimer(int value);

    boolean isGroundPounding();
    GroundPoundType getGroundPoundType();
    void startGroundPound(ServerWorld world, PlayerEntity user, GroundPoundType groundPoundType);
    void endGroundPound(ServerWorld world, PlayerEntity user, @Nullable Entity target);
    void cancelGroundPound(PlayerEntity user);
}
