package net.moss.resonance.util.interfaces;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface CommandableEntityInterface {
    void command(World world, PlayerEntity commander);
    void commandToItem(World world, PlayerEntity commander, ItemEntity target);
    void commandToBlock(World world, PlayerEntity commander, BlockPos target);
    void commandToEntity(World world, PlayerEntity commander, Entity target);
}
