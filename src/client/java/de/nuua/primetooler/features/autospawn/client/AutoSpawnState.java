package de.nuua.primetooler.features.autospawn.client;

/**
 * WHY: Central toggle for auto-spawn safety behavior.
 * PERF: O(1) checks, no allocations.
 */
public final class AutoSpawnState {
	private static boolean enabled;

	private AutoSpawnState() {
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
