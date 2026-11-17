package net.moss.resonance.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.BlockHitResult;
import net.moss.resonance.util.interfaces.MouseOverHudInterface;

@Environment(EnvType.CLIENT)
public class HudRendering {
	public static void hudRender(DrawContext ctx, MinecraftClient instance) {
        if(!MinecraftClient.isHudEnabled() || instance.currentScreen != null || instance.player.isSpectator() || !(instance.crosshairTarget instanceof BlockHitResult result) || !(instance.world.getBlockEntity(result.getBlockPos()) instanceof MouseOverHudInterface blockEntity)) return;
        final int x = instance.getWindow().getScaledWidth() / 2;
        final int y = instance.getWindow().getScaledHeight() / 2;
		blockEntity.drawTooltip(ctx, instance, result, x, y);
	}
}