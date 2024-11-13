package net.moss.resonance.mixin;

import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.InGameHud.HeartType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.moss.resonance.Resonance;
import net.moss.resonance.util.interfaces.ClientPlayerEntityInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Unique private static final Identifier CUSTOM_HEARTS = Identifier.of(Resonance.MOD_ID, "textures/gui/custom_hearts.png");
    @Unique private static final Identifier NARROW_VISION = Identifier.of(Resonance.MOD_ID, "textures/gui/narrow_vision.png");

    @Inject(method = "drawHeart", at = @At("HEAD"), cancellable = true)
    private void drawHeart(DrawContext context, HeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!blinking && type == HeartType.NORMAL && client.cameraEntity instanceof PlayerEntity player) {
            int offset = 0;
            if (player.hasStatusEffect(Resonance.SOUL_FATIGUE)) { offset = 0; }
            else if (player.hasStatusEffect(Resonance.SPARKING)) { offset = 13; }
            else { return; }
            context.drawTexture(CUSTOM_HEARTS, x-2, y-2, (half ? 13 : 0) + (hardcore ? 26 : 0), offset, 13, 13);
            ci.cancel();
        }
    }

    @Inject(method = "renderMiscOverlays", at = @At("HEAD"), cancellable = true)
    private void drawHeart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        int i = context.getScaledWindowWidth();
        int j = context.getScaledWindowHeight();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player.hasStatusEffect(Resonance.CRUSHING_WEIGHT)) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            context.setShaderColor(1.0f, 1.0f, 1.0f, MathHelper.lerp(tickCounter.getTickDelta(false), ((ClientPlayerEntityInterface)player).getPrevCrushingWeightIntensity(), ((ClientPlayerEntityInterface)player).getCrushingWeightIntensity()));
            context.drawTexture(NARROW_VISION, 0, 0, -90, 0.0f, 0.0f, i, j, i, j);
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    }
}
