package de.nuua.primetooler.features.playermark.client;

/**
 * WHY: Client-only toggle for special name decorations (tab/chat/nametags).
 * PERF: Simple boolean guard on hotpaths.
 */
public final class SpecialNamesState {
	private static boolean enabled = true;

	private SpecialNamesState() {
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
