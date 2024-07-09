package net.moss.resonance.particle;

import net.minecraft.particle.SimpleParticleType;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.moss.resonance.Resonance;

public class ModParticles {

	public static final SimpleParticleType SMALL_RING_PARTICLE = FabricParticleTypes.simple();
	public static final SimpleParticleType MEDIUM_RING_PARTICLE = FabricParticleTypes.simple();
	public static final SimpleParticleType SPARK_PARTICLE = FabricParticleTypes.simple();
	public static final SimpleParticleType AMETHYST_PARTICLE = FabricParticleTypes.simple();
    public static final SimpleParticleType GOLDEN_GLITTER = FabricParticleTypes.simple();

    public static void registerModParticles() {
		Registry.register(Registries.PARTICLE_TYPE, Identifier.of(Resonance.MOD_ID, "small_ring_particle"), SMALL_RING_PARTICLE);
		Registry.register(Registries.PARTICLE_TYPE, Identifier.of(Resonance.MOD_ID, "medium_ring_particle"), MEDIUM_RING_PARTICLE);
		Registry.register(Registries.PARTICLE_TYPE, Identifier.of(Resonance.MOD_ID, "spark_particle"), SPARK_PARTICLE);
		Registry.register(Registries.PARTICLE_TYPE, Identifier.of(Resonance.MOD_ID, "amethyst_particle"), AMETHYST_PARTICLE);
		Registry.register(Registries.PARTICLE_TYPE, Identifier.of(Resonance.MOD_ID, "golden_glitter"), GOLDEN_GLITTER);
	}
}