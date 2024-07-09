package net.moss.resonance.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.moss.resonance.util.Utils;

public class BackRenderItem extends Item implements Equipment {
    public BackRenderItem(Settings settings) { super(settings); }
    @Override public EquipmentSlot getSlotType() { return EquipmentSlot.CHEST; }
    
    @Override public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return Utils.trinketHotswap(world, user, hand, this);
    }
}