package de.nuua.primetooler.features.camerazoom.client;

/**
 * WHY: Client-only toggle to skip the front camera perspective.
 * PERF: Simple boolean guard in the F5 cycle path.
 */
public final class FrontCameraToggleState {
	private static boolean disabled;

	private FrontCameraToggleState() {
	}

	public static boolean isDisabled() {
		return disabled;
	}

	public static void setDisabled(boolean value) {
		disabled = value;
	}

	public static boolean toggleDisabled() {
		disabled = !disabled;
		return disabled;
	}
}
