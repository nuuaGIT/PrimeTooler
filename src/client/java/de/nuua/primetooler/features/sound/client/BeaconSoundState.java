package de.nuua.primetooler.features.sound.client;

/**
 * WHY: Client-Option zum Stummschalten von Booster-/Beacon-Sounds.
 * PERF: Ein Boolean-Check pro Sound-Play.
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
