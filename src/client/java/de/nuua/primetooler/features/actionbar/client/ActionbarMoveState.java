package de.nuua.primetooler.features.actionbar.client;

/**
 * WHY: Client-only toggle for moving the actionbar overlay via the HUD editor.
 * PERF: Single boolean check in the actionbar render hook.
 */
public final class ActionbarMoveState {
	private static boolean enabled;

	private ActionbarMoveState() {
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

