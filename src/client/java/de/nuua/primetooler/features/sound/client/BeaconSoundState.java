package de.nuua.primetooler.features.sound.client;

/**
 * WHY: Client-only toggle for muting beacon event sounds.
 * PERF: One boolean check per sound play.
 */
public final class BeaconSoundState {
	private static boolean enabled;

	private BeaconSoundState() {
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
