package net.moss.resonance.block.entity;

import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.moss.resonance.OtherOrbEffectsRecipe;
import net.moss.resonance.Resonance;
import net.moss.resonance.block.PearlHub;
import net.moss.resonance.util.Utils;

public class PearlHubBlockEntity extends BlockEntity implements Inventory {
    ItemStack pearl = ItemStack.EMPTY;
    Identifier texture;
    
    public PearlHubBlockEntity(BlockPos pos, BlockState state) {
        super(Resonance.PEARL_HUB_BLOCK_ENTITY, pos, state);
    }
    
    public PearlHubBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    
    public void setPearl(ItemStack pearl) {
        this.pearl = pearl;
        if (pearl.isEmpty()) {
            this.texture = Identifier.of("");
            return;
        }
        Optional<RecipeEntry<OtherOrbEffectsRecipe>> recipe = world.getServer().getRecipeManager().getFirstMatch(OtherOrbEffectsRecipe.Type.INSTANCE, new SingleStackRecipeInput(pearl), world);
        if (recipe.isPresent()) { this.texture = Identifier.of(recipe.get().value().getTexture() + ".png"); }
        else { this.texture = Identifier.of(""); }
    }

    public boolean hasPearl() { return !this.pearl.isEmpty(); }
    public ItemStack getPearl() { return this.pearl; }
    public Identifier getTexture() { return this.texture; }
    public void clear() { this.setPearl(ItemStack.EMPTY); }
    void onBookRemoved() { PearlHub.setHasPearl(null, this.getWorld(), this.getPos(), this.getCachedState(), false); }
    
    @Nullable @Override public Packet<ClientPlayPacketListener> toUpdatePacket() { return BlockEntityUpdateS2CPacket.create(this); }
    @Override public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) { return createNbt(registryLookup); }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.pearl = nbt.contains("Pearl", NbtElement.COMPOUND_TYPE) ? ItemStack.fromNbt(registryLookup, nbt.getCompound("Pearl")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        this.texture = nbt.contains("Texture", NbtElement.STRING_TYPE) ? Identifier.of(nbt.getString("Texture")) : null;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        if (hasPearl()) {
            nbt.put("Pearl", this.getPearl().encode(registryLookup));
            nbt.putString("Texture", this.getTexture() != null ? this.getTexture().toString() : "");
        }
    }

    public int usePearl(World world, BlockPos pos, PlayerEntity player) {
        if (!world.isClient && hasPearl()) {
            Resonance.PEARL_HUB_CRITERION.trigger((ServerPlayerEntity) player, getPearl());
            return affectEntities(world, pos, List.of(player));
        }
        return 3;
    }

    public int pulsePearl(World world, BlockPos pos) {
        if (!world.isClient && hasPearl()) {
            world.emitGameEvent(null, GameEvent.BLOCK_ACTIVATE, pos);
            world.playSound(null, pos, SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK, SoundCategory.BLOCKS, 0.7f, 0.6f + world.random.nextFloat() * 0.4f);
            return affectEntities(world, pos, Utils.entitiesInRadius(world, this.getPos().toCenterPos(), null, 3, 3, Entity::isAlive));
        }
        return 3;
    }

    public int affectEntities(World world, BlockPos pos, List<Entity> entities) {
        int recuperation = 5;
        int duration = 3;
        int strength = 3;
        if (pearl.contains(Resonance.PEARL_STATS)) {
            List<Integer> pearlStats = pearl.get(Resonance.PEARL_STATS);
            duration = pearlStats.get(0);
            recuperation = pearlStats.get(1);
            strength = pearlStats.get(2);
        }
        if (pearl.contains(DataComponentTypes.POTION_CONTENTS)) {
            for (StatusEffectInstance effect : pearl.get(DataComponentTypes.POTION_CONTENTS).getEffects()) {
                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity livingEntity) {
                        livingEntity.addStatusEffect(new StatusEffectInstance(effect.getEffectType(), effect.getDuration() / (4 - duration), (int) Math.max(0, effect.getAmplifier() / (4 - strength))));
                    }
                }
            }
        }
        Optional<RecipeEntry<OtherOrbEffectsRecipe>> recipe = world.getServer().getRecipeManager().getFirstMatch(OtherOrbEffectsRecipe.Type.INSTANCE, new SingleStackRecipeInput(pearl), world);
        if (recipe.isPresent()) {
            OtherOrbEffectsRecipe recipeValue = recipe.get().value();
            ItemStack item = recipeValue.getItem();
            if (item.contains(DataComponentTypes.POTION_CONTENTS)) {
                item.get(DataComponentTypes.POTION_CONTENTS).forEachEffect(effect -> {
                    StatusEffectInstance newEffect = new StatusEffectInstance(effect.getEffectType(), effect.getDuration(), effect.getAmplifier());
                    for (Entity entity : entities) {
                        if (entity instanceof LivingEntity livingEntity) {
                            livingEntity.addStatusEffect(newEffect);
                        }
                    }
                });
            }
            if (!recipeValue.getSound().equals(Resonance.PEARL_HUB_USE_SOUND.value())) {
                world.playSound(null, pos, recipeValue.getSound(), SoundCategory.BLOCKS);
            }
        }

        if (pearl.isOf(Items.ENDER_PEARL)) {
            for (Entity entity : entities) {
                if (!world.isClient && entity instanceof LivingEntity livingEntity) {
                    for (int i = 0; i < 16; ++i) {
                        SoundCategory soundCategory;
                        SoundEvent soundEvent;
                        double d = livingEntity.getX() + (world.getRandom().nextDouble() - 0.5) * 16.0;
                        double e = MathHelper.clamp(livingEntity.getY() + (double)(world.getRandom().nextInt(16) - 8), (double)world.getBottomY(), (double)(world.getBottomY() + ((ServerWorld)world).getLogicalHeight() - 1));
                        double f = livingEntity.getZ() + (world.getRandom().nextDouble() - 0.5) * 16.0;
                        if (livingEntity.hasVehicle()) {
                            livingEntity.stopRiding();
                        }
                        Vec3d vec3d = livingEntity.getPos();
                        if (!livingEntity.teleport(d, e, f, true)) continue;
                        world.emitGameEvent(GameEvent.TELEPORT, vec3d, GameEvent.Emitter.of(livingEntity));
                        if (livingEntity instanceof FoxEntity) {
                            soundEvent = SoundEvents.ENTITY_FOX_TELEPORT;
                            soundCategory = SoundCategory.NEUTRAL;
                        } else {
                            soundEvent = SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT;
                            soundCategory = SoundCategory.PLAYERS;
                        }
                        world.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), soundEvent, soundCategory);
                        livingEntity.onLanding();
                        break;
                    }
                }
            }
        }

        return recuperation;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(null, player);
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.getPearl();
    }

    @Override
    public boolean isEmpty() {
        return !this.hasPearl();
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack stack = this.getPearl();
        clear();
        PearlHub.setHasPearl(null, world, pos, getCachedState(), false);
        return stack;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return removeStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        setPearl(stack);
        PearlHub.setHasPearl(null, world, pos, getCachedState(), true);
    }

    @Override
    public int size() {
        return 1;
    }
}
