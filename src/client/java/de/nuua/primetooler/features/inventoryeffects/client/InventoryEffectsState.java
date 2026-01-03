package de.nuua.primetooler.features.inventoryeffects.client;

/**
 * WHY: Client-only toggle for the inventory effects overlay.
 * PERF: Single boolean lookup in render hook.
 */
public final class InventoryEffectsState {
	private static boolean enabled = true;

	private InventoryEffectsState() {
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static boolean toggleEnabled() {
		enabled = !enabled;
		return enabled;
	}

	public static void setEnabled(boolean value) {
		enabled = value;
	}
}
