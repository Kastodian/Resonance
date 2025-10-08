package net.moss.resonance.util;

import net.minecraft.util.StringIdentifiable;

public enum VentThickness implements StringIdentifiable {
	TIP("tip"),
	FRUSTUM("frustum"),
	MIDDLE("middle"),
	BASE("base");

	private final String name;

	private VentThickness(final String name) {
		this.name = name;
	}

	@Override
	public String asString() {
		return this.name;
	}
}