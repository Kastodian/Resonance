package net.moss.resonance.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.moss.resonance.Resonance;
import net.moss.resonance.util.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow protected abstract boolean hasLabel(Entity entity);

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;hasLabel(Lnet/minecraft/entity/Entity;)Z"))
    private boolean renderNametag(EntityRenderer<Entity> instance, Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            if (Utils.isItemEquipped(livingEntity, Resonance.PHANTOM_SCARF, false)) {
                return false;
            }
        }
        return this.hasLabel(entity);
    }
}