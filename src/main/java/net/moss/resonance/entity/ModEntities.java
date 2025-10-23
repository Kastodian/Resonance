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
	public static final EntityType<PearlProjectileEntity> PEARL_PROJECTILE = Registry.register(Registries.ENTITY_TYPE, Identifier.of(Resonance.MOD_ID, "pearl_projectile"), FabricEntityTypeBuilder.<PearlProjectileEntity>create(SpawnGroup.MISC, PearlProjectileEntity::new).dimensions(EntityDimensions.fixed(0.25F, 0.25F)).trackRangeChunks(4).trackedUpdateRate(10).build());
	public static final EntityType<LostSoulEntity> LOST_SOUL = Registry.register(Registries.ENTITY_TYPE, Identifier.of(Resonance.MOD_ID, "lost_soul"), FabricEntityTypeBuilder.<LostSoulEntity>create(SpawnGroup.MONSTER, LostSoulEntity::new).dimensions(EntityDimensions.fixed(0.6F, 0.6F)).fireImmune().trackRangeChunks(4).build());
	public static final EntityType<VentFluidProjectileEntity> VENT_FLUID_PROJECTILE = Registry.register(Registries.ENTITY_TYPE, Identifier.of(Resonance.MOD_ID, "vent_fluid_projectile"), FabricEntityTypeBuilder.<VentFluidProjectileEntity>create(SpawnGroup.MISC, VentFluidProjectileEntity::new).dimensions(EntityDimensions.fixed(0.5F, 0.5F)).trackRangeChunks(4).trackedUpdateRate(10).build());
	public static final EntityType<VolcanoSnailEntity> VOLCANO_SNAIL = Registry.register(Registries.ENTITY_TYPE, Identifier.of(Resonance.MOD_ID, "volcano_snail"), FabricEntityTypeBuilder.<VolcanoSnailEntity>create(SpawnGroup.WATER_AMBIENT, VolcanoSnailEntity::new).dimensions(EntityDimensions.fixed(0.4F, 0.4F)).trackRangeChunks(4).build());
	public static final EntityType<BeaconedPearlEntity> BEACONED_PEARL = Registry.register(Registries.ENTITY_TYPE, Identifier.of(Resonance.MOD_ID, "beaconed_pearl"), FabricEntityTypeBuilder.<BeaconedPearlEntity>create(SpawnGroup.WATER_AMBIENT, BeaconedPearlEntity::new).dimensions(EntityDimensions.fixed(0.6F, 0.6F)).trackRangeChunks(4).build());

	public static void registerModEntities() {
		FabricDefaultAttributeRegistry.register(LOST_SOUL, LostSoulEntity.createLostSoulAttributes());
		FabricDefaultAttributeRegistry.register(VOLCANO_SNAIL, VolcanoSnailEntity.createVolcanoSnailAttributes());
		FabricDefaultAttributeRegistry.register(BEACONED_PEARL, BeaconedPearlEntity.createLostSoulAttributes());
	}
}