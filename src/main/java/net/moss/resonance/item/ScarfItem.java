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
import net.moss.resonance.util.interfaces.ScarfInterface;

public class ScarfItem extends Item implements Equipment, ScarfInterface {
	public String TEXTURE;

	public ScarfItem(Settings settings, String modId, String texture) {
		super(settings.maxCount(1));
		TEXTURE = modId + ":textures/models/armor/" + texture;
	}

	@Override public EquipmentSlot getSlotType() { return EquipmentSlot.CHEST; }
	@Override public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) { return Utils.trinketHotswap(world, user, hand, this); }
	@Override public String getTexture() { return TEXTURE; }
}