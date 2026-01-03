package de.nuua.primetooler.features.locatorbar.client;

/**
 * WHY: Client-only toggle for the locator bar overlay.
 * PERF: Single boolean lookup in render hook.
 */
public final class LocatorBarState {
	private static boolean enabled = true;

	private LocatorBarState() {
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
