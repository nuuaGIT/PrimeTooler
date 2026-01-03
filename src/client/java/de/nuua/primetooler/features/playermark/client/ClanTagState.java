package de.nuua.primetooler.features.playermark.client;

/**
 * WHY: Client-only toggle for hiding clan tags in tab list and name tags.
 * PERF: One boolean check per render call.
 */
public final class ClanTagState {
	private static boolean enabled;

	private ClanTagState() {
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
