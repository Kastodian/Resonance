package net.moss.resonance.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.moss.resonance.Resonance;

@SuppressWarnings("deprecation")
public class ModEntities {
    public static final EntityType<WhistlingArrowEntity> WHISTLING_ARROW = Registry.register(Registries.ENTITY_TYPE, Identifier.of(Resonance.MOD_ID, "whistling_arrow"), FabricEntityTypeBuilder.<WhistlingArrowEntity>create(SpawnGroup.MISC, WhistlingArrowEntity::new).dimensions(EntityDimensions.fixed(0.5F, 0.5F)).trackRangeChunks(4).trackedUpdateRate(10).build());
    public static final EntityType<SculkProjectileEntity> SCULK_PROJECTILE = Registry.register(Registries.ENTITY_TYPE, Identifier.of(Resonance.MOD_ID, "sculk_projectile"), FabricEntityTypeBuilder.<SculkProjectileEntity>create(SpawnGroup.MISC, SculkProjectileEntity::new).dimensions(EntityDimensions.fixed(0.3F, 0.3F)).trackRangeChunks(4).trackedUpdateRate(10).build());
    public static final EntityType<LostSoulEntity> LOST_SOUL = Registry.register(Registries.ENTITY_TYPE, Identifier.of(Resonance.MOD_ID, "lost_soul"), FabricEntityTypeBuilder.<LostSoulEntity>create(SpawnGroup.MONSTER, LostSoulEntity::new).dimensions(EntityDimensions.fixed(0.6F, 0.6F)).fireImmune().build());

    public static void registerModEntities() {
        FabricDefaultAttributeRegistry.register(LOST_SOUL, LostSoulEntity.createLostSoulAttributes());
    }
}