package de.nuua.primetooler.features.autospawn.client;

/**
 * WHY: Central toggle for auto-spawn safety behavior.
 * PERF: O(1) checks, no allocations.
 */
public final class AutoSpawnState {
	public static final float DEFAULT_HEARTS_THRESHOLD = 2.5f;
	private static boolean enabled;
	private static float heartsThreshold = DEFAULT_HEARTS_THRESHOLD;

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

	public static float heartsThreshold() {
		return heartsThreshold;
	}

	public static void setHeartsThreshold(float value) {
		if (!Float.isFinite(value) || value <= 0.0f) {
			heartsThreshold = DEFAULT_HEARTS_THRESHOLD;
			return;
		}
		heartsThreshold = value;
	}
}
