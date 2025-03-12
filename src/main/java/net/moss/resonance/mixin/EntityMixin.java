package net.moss.resonance.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World.ExplosionSourceType;
import net.moss.resonance.Resonance;
import net.moss.resonance.particle.ModParticles;
import net.moss.resonance.recipe.LightningStrikeRecipe;
import java.util.Optional;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "onStruckByLightning", at = @At("HEAD"), cancellable = true)
    private void onStruckByLightning(ServerWorld world, LightningEntity lightning, CallbackInfo callBackInfo) {
        Entity entity = (Entity)((Object) this);
        if (entity instanceof ItemEntity itemEntity) {
            boolean cancel = false;
            if (itemEntity.cannotPickup()) { cancel = true; }
            if (itemEntity.getStack().isIn(Resonance.CONDUCTIVE_ITEMS_TAG) || itemEntity.getSteppingBlockState().isIn(Resonance.CONDUCTIVE_BLOCKS_TAG)) {
                ItemStack material = itemEntity.getStack();
                Optional<RecipeEntry<LightningStrikeRecipe>> recipe = world.getRecipeManager().getFirstMatch(LightningStrikeRecipe.Type.INSTANCE, new SingleStackRecipeInput(material), world);
                if (recipe.isPresent()) {
                    world.spawnParticles(ModParticles.SPARK_PARTICLE, itemEntity.getX(), itemEntity.getY() + itemEntity.getHeight()/2, itemEntity.getZ(), 20, 0, 0, 0, 1);
                    LightningStrikeRecipe lightningStrikeRecipe = recipe.get().value();
                    int explosionPower = lightningStrikeRecipe.getExplosionPower();
                    if (explosionPower > 0) { world.createExplosion(lightning, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), explosionPower, ExplosionSourceType.MOB); }
                    for (ItemStack stack : lightningStrikeRecipe.getResults()) {
                        ItemEntity resultItem = new ItemEntity(world, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), stack.copy(), 0, 0.2, 0);
                        resultItem.setToDefaultPickupDelay();
                        world.spawnEntity(resultItem);
                    }
                    if (material.getCount() > 0 && lightningStrikeRecipe.getIsMaterialConsumed()) { material.decrement(1); }
                    else { itemEntity.addVelocity(0, 0.1, 0); }
                    cancel = true;
                }
            }
            if (cancel) { callBackInfo.cancel(); }
        }
    }
}
