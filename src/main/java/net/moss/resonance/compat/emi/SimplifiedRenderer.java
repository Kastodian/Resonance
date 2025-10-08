package net.moss.resonance.compat.emi;

import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.moss.resonance.Resonance;

public class SimplifiedRenderer implements EmiRenderable {
	private final int u;
	private final int v;
	private final Identifier SPRITE_SHEET = Identifier.of(Resonance.MOD_ID, "textures/gui/emi_simplified_textures.png");

	public SimplifiedRenderer(int u, int v) {
		this.u = u;
		this.v = v;
	}

	@Override
	public void render(DrawContext draw, int x, int y, float delta) {
		draw.drawTexture(SPRITE_SHEET, x, y, u, v, 16, 16, 32, 32);
	}
}