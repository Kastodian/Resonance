package net.moss.resonance;

import java.util.Optional;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.util.Identifier;
import net.moss.resonance.block.renderer.RainChimeBlockRenderer;
import net.moss.resonance.block.entity.TintedBottleBlockEntity;
import net.moss.resonance.block.renderer.BrittleBlockEntityRenderer;
import net.moss.resonance.block.renderer.BuddingPearlIceBlockEntityRenderer;
import net.moss.resonance.block.renderer.PearlHubBlockEntityRenderer;
import net.moss.resonance.entity.ModEntities;
import net.moss.resonance.entity.client.BeaconedPearlModel;
import net.moss.resonance.entity.client.BeaconedPearlRenderer;
import net.moss.resonance.entity.client.LostSoulModel;
import net.moss.resonance.entity.client.LostSoulRenderer;
import net.moss.resonance.entity.client.SculkProjectileRenderer;
import net.moss.resonance.entity.client.VentFluidProjectileRenderer;
import net.moss.resonance.entity.client.VolcanoSnailModel;
import net.moss.resonance.entity.client.YetiCrabModel;
import net.moss.resonance.entity.client.YetiCrabRenderer;
import net.moss.resonance.entity.client.VolcanoSnailRenderer;
import net.moss.resonance.entity.client.WhistlingArrowRenderer;
import net.moss.resonance.particle.ModParticles;
import net.moss.resonance.particle.SmallRingParticle;
import net.moss.resonance.particle.SoulDarknessParticle;
import net.moss.resonance.particle.VentSmokeParticle;
import net.moss.resonance.world.DreamSweetSkyRenderer;
import net.moss.resonance.particle.CustomGlowParticle;
import net.moss.resonance.particle.MediumRingParticle;

public class ResonanceClient implements ClientModInitializer {
	public static final EntityModelLayer LOST_SOUL_LAYER = new EntityModelLayer(Identifier.of(Resonance.MOD_ID, "lost_soul"), "main");
	public static final EntityModelLayer VOCANO_SNAIL_LAYER = new EntityModelLayer(Identifier.of(Resonance.MOD_ID, "volcano_snail"), "main");
	public static final EntityModelLayer YETI_CRAB_LAYER = new EntityModelLayer(Identifier.of(Resonance.MOD_ID, "yeti_crab"), "main");
	public static final EntityModelLayer BEACONED_PEARL_LAYER = new EntityModelLayer(Identifier.of(Resonance.MOD_ID, "beaconed_pearl"), "main");

	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.SMALL_CHARGED_AMETHYST_BUD, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.MEDIUM_CHARGED_AMETHYST_BUD, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.LARGE_CHARGED_AMETHYST_BUD, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.CHARGED_AMETHYST_CLUSTER, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.TREASURE_HOARD, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.BOOK_PILE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.COMPOST_HEAP, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.TINTED_BOTTLE_BLOCK, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.SCULK_BOTTLE_BLOCK, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.SOUL_BOTTLE_BLOCK, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.FIREBALL_BOTTLE_BLOCK, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.LIGHTNING_BOTTLE_BLOCK, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.SHULKER_BOTTLE_BLOCK, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.VENT_FLUID_BOTTLE_BLOCK, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.JUMP_ORB_NORMAL, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.JUMP_ORB_STRONG, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.PEARL_HUB, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.BUDDING_PEARL_ICE, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.ICICLE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.ICE_FUR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.BRITTLE_BLOCK, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.BATHYAL_VENT, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.VENT_FLUID, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.TUBE_WORMS, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.WALL_TUBE_WORMS, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Resonance.DATAPACK_BLOCK, RenderLayer.getCutout());

		ParticleFactoryRegistry.getInstance().register(ModParticles.SMALL_RING_PARTICLE, SmallRingParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.MEDIUM_RING_PARTICLE, MediumRingParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.SPARK_PARTICLE, CustomGlowParticle.SparkFactory::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.AMETHYST_PARTICLE, CustomGlowParticle.AmethystFactory::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.GOLDEN_GLITTER, CustomGlowParticle.GoldenGlitter::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.VENT_SMOKE, VentSmokeParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.SOUL_DARKNESS, SoulDarknessParticle.Factory::new);

		EntityRendererRegistry.register(ModEntities.WHISTLING_ARROW, WhistlingArrowRenderer::new);
		EntityRendererRegistry.register(ModEntities.SCULK_PROJECTILE, SculkProjectileRenderer::new);
		EntityRendererRegistry.register(ModEntities.LOST_SOUL, LostSoulRenderer::new);
		EntityRendererRegistry.register(ModEntities.PEARL_PROJECTILE, FlyingItemEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.VENT_FLUID_PROJECTILE, VentFluidProjectileRenderer::new);
		EntityRendererRegistry.register(ModEntities.VOLCANO_SNAIL, VolcanoSnailRenderer::new);
		EntityRendererRegistry.register(ModEntities.YETI_CRAB, YetiCrabRenderer::new);
		EntityRendererRegistry.register(ModEntities.BEACONED_PEARL, BeaconedPearlRenderer::new);

		EntityModelLayerRegistry.registerModelLayer(LOST_SOUL_LAYER, LostSoulModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(VOCANO_SNAIL_LAYER, VolcanoSnailModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(YETI_CRAB_LAYER, YetiCrabModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(BEACONED_PEARL_LAYER, BeaconedPearlModel::getTexturedModelData);

		BlockEntityRendererFactories.register(Resonance.RAIN_CHIME_BLOCK_ENTITY, RainChimeBlockRenderer::new);
		BlockEntityRendererFactories.register(Resonance.PEARL_HUB_BLOCK_ENTITY, PearlHubBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(Resonance.BUDDING_PEARL_ICE_BLOCK_ENTITY, BuddingPearlIceBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(Resonance.BRITTLE_BLOCK_ENTITY, BrittleBlockEntityRenderer::new);

		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex > 0 ? -1 : (stack.contains(DataComponentTypes.DYED_COLOR) ? DyedColorComponent.getColor(stack, 16777215) : -1), Resonance.SCARF, Resonance.VENT_FLUID_BOTTLE);
		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> tintIndex == 0 ? BiomeColors.getGrassColor(world, pos) : -1, Resonance.COMPOST_HEAP);
		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
			Optional<TintedBottleBlockEntity> blockEntity = world.getBlockEntity(pos, Resonance.TINTED_BOTTLE_BLOCK_ENTITY);
			return blockEntity.isPresent() && tintIndex == 0 ? blockEntity.get().getColor() : -1;
		}, Resonance.VENT_FLUID_BOTTLE_BLOCK);

		DimensionRenderingRegistry.registerSkyRenderer(Resonance.DREAM_SWEET, new DreamSweetSkyRenderer());
	}
}