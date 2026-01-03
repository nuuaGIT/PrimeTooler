package de.nuua.primetooler.features.chatmention.client;

/**
 * WARUM: Client-Only Toggle fuer Chat-Markierungen.
 * PERF: Einfache Bool-Pruefung im Chat-Hotpath.
 */
public final class ChatMentionState {
	private static boolean enabled;

	private ChatMentionState() {
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
