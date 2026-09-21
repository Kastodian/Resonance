package net.moss.resonance.entity;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BiomeKeys;
import net.moss.resonance.Resonance;

public class ModEntities {
	public static final EntityType<WhistlingArrowEntity> WHISTLING_ARROW = Registry.register(
			Registries.ENTITY_TYPE, Identifier.of(Resonance.MOD_ID, "whistling_arrow"),
			EntityType.Builder.<WhistlingArrowEntity>create(WhistlingArrowEntity::new, SpawnGroup.MISC)
					.dimensions(0.5F, 0.5F)
					.trackingTickInterval(10)
					.maxTrackingRange(4)
					.build());

	public static final EntityType<SculkProjectileEntity> SCULK_PROJECTILE = Registry.register(
			Registries.ENTITY_TYPE, Identifier.of(Resonance.MOD_ID, "sculk_projectile"),
			EntityType.Builder.<SculkProjectileEntity>create(SculkProjectileEntity::new, SpawnGroup.MISC)
					.dimensions(0.3F, 0.3F)
					.trackingTickInterval(10)
					.maxTrackingRange(4)
					.build());

	public static final EntityType<PearlProjectileEntity> PEARL_PROJECTILE = Registry.register(
			Registries.ENTITY_TYPE, Identifier.of(Resonance.MOD_ID, "pearl_projectile"),
			EntityType.Builder.<PearlProjectileEntity>create(PearlProjectileEntity::new, SpawnGroup.MISC)
					.dimensions(0.25F, 0.25F)
					.trackingTickInterval(10)
					.maxTrackingRange(4)
					.build());

	public static final EntityType<VentFluidProjectileEntity> VENT_FLUID_PROJECTILE = Registry.register(
			Registries.ENTITY_TYPE, Identifier.of(Resonance.MOD_ID, "vent_fluid_projectile"),
			EntityType.Builder.<VentFluidProjectileEntity>create(VentFluidProjectileEntity::new, SpawnGroup.MISC)
					.dimensions(0.5F, 0.5F)
					.trackingTickInterval(10)
					.maxTrackingRange(4)
					.build());

	public static final EntityType<BeaconedPearlEntity> BEACONED_PEARL = Registry.register(
			Registries.ENTITY_TYPE, Identifier.of(Resonance.MOD_ID, "beaconed_pearl"),
			EntityType.Builder.<BeaconedPearlEntity>create(BeaconedPearlEntity::new, SpawnGroup.MISC)
					.dimensions(0.6F, 0.6F)
					.trackingTickInterval(10)
					.maxTrackingRange(4)
					.build());

	public static final EntityType<LostSoulEntity> LOST_SOUL = Registry.register(
			Registries.ENTITY_TYPE, Identifier.of(Resonance.MOD_ID, "lost_soul"),
			EntityType.Builder.<LostSoulEntity>create(LostSoulEntity::new, SpawnGroup.MONSTER)
					.dimensions(0.6F, 0.6F)
					.maxTrackingRange(8)
					.makeFireImmune()
					.build());

	public static final EntityType<VolcanoSnailEntity> VOLCANO_SNAIL = Registry.register(
			Registries.ENTITY_TYPE, Identifier.of(Resonance.MOD_ID, "volcano_snail"),
			EntityType.Builder.<VolcanoSnailEntity>create(VolcanoSnailEntity::new, SpawnGroup.WATER_AMBIENT)
					.dimensions(0.4F, 0.4F)
					.maxTrackingRange(8)
					.build());

	public static final EntityType<YetiCrabEntity> YETI_CRAB = Registry.register(
			Registries.ENTITY_TYPE, Identifier.of(Resonance.MOD_ID, "yeti_crab"),
			EntityType.Builder.<YetiCrabEntity>create(YetiCrabEntity::new, SpawnGroup.WATER_AMBIENT)
					.dimensions(0.4F, 0.4F)
					.maxTrackingRange(8)
					.build());

	public static void registerModEntities() {
		FabricDefaultAttributeRegistry.register(BEACONED_PEARL, BeaconedPearlEntity.createLostSoulAttributes());
		FabricDefaultAttributeRegistry.register(LOST_SOUL, LostSoulEntity.createLostSoulAttributes());
		FabricDefaultAttributeRegistry.register(VOLCANO_SNAIL, VolcanoSnailEntity.createVolcanoSnailAttributes());
		FabricDefaultAttributeRegistry.register(YETI_CRAB, YetiCrabEntity.createVolcanoSnailAttributes());
		
		BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.SOUL_SAND_VALLEY), SpawnGroup.MONSTER, LOST_SOUL, 5, 1, 2);
		SpawnRestriction.register(LOST_SOUL, SpawnLocationTypes.UNRESTRICTED, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
	}
}