package de.nuua.primetooler.features.terminalstackcount.client;

/**
 * WHY: Control terminal-style stack-count overlays.
 * PERF: Single boolean check at item decoration render time.
 */
public final class TerminalStackCountState {
	private static boolean enabled;
	public static float OVERLAY_SCALE = 0.66f;
	public static int OVERLAY_COLOR = 0xFFFFFF55;
	public static float OVERLAY_NUDGE_Y = -2.0f;

	private TerminalStackCountState() {
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
