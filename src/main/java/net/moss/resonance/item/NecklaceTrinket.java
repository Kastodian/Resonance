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

public class NecklaceTrinket extends Item implements Equipment {
    public String TEXTURE;

    public NecklaceTrinket(Settings settings, String modId, String texture) {
        super(settings.maxCount(1));
        TEXTURE = modId + ":textures/models/armor/" + texture;
    }

    public int getSpecial(ItemStack stack) {
        return 0;
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.CHEST;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return Utils.trinketHotswap(world, user, hand, this);
    }
}