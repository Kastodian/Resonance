package net.moss.resonance.item;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.moss.resonance.util.interfaces.ShieldInterface;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

public class CustomShield extends Item implements ShieldInterface {
    private int coolDownTicks;
    private int enchantability;
    private Item repairItems;

    public CustomShield(Settings settings, int coolDownTicks, int enchantability, Item repairItem) {
        super(settings);
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSER_BEHAVIOR);
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            this.RegisterModelPredicate();
        }
        this.coolDownTicks = coolDownTicks;
        this.enchantability = enchantability;
        this.repairItems = repairItem;
    }

    private void RegisterModelPredicate() {
        ModelPredicateProviderRegistry.register(Identifier.of("blocking"), (itemStack, clientWorld, livingEntity, i) -> {
            return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getActiveItem() == itemStack ? 1.0F : 0.0F;
        });
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        user.setCurrentHand(hand);
        return TypedActionResult.consume(itemStack);
    }

    @Override public int getCoolDownTicks() { return this.coolDownTicks; }
    @Override public UseAction getUseAction(ItemStack stack) { return UseAction.BLOCK; }
    @Override public int getMaxUseTime(ItemStack stack, LivingEntity user) { return 72000; }
    @Override public boolean canRepair(ItemStack stack, ItemStack ingredient) { return ingredient.getItem() == repairItems || super.canRepair(stack, ingredient); }
    @Override public boolean isEnchantable(ItemStack stack) { return !stack.hasEnchantments(); }
    @Override public int getEnchantability() { return this.enchantability; }
    public void setCoolDownTicks(int coolDownTicks) { this.coolDownTicks = coolDownTicks; }
    public void setEnchantability(int enchantability) { this.enchantability = enchantability; }
}