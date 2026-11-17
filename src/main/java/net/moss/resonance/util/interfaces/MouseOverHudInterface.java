package net.moss.resonance.util.interfaces;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.BlockHitResult;

public interface MouseOverHudInterface {
	void drawTooltip(DrawContext ctx, MinecraftClient instance, BlockHitResult result, int x, int y);
}