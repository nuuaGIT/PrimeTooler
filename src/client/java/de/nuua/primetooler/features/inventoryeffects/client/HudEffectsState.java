package de.nuua.primetooler.features.inventoryeffects.client;

/**
 * WHY: Toggle the HUD effects overlay without touching server state.
 * PERF: Single volatile boolean, read on render only.
 */
public final class HudEffectsState {
	private static boolean enabled = true;

	private HudEffectsState() {
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void setEnabled(boolean value) {
		enabled = value;
	}

	public static boolean toggleEnabled() {
		enabled = !enabled;
		return enabled;
	}
}
