package de.nuua.primetooler.features.sound.client;

/**
 * WHY: Client-Option zum Stummschalten des Jackpot-Sounds.
 * PERF: Ein Boolean-Check pro Sound-Play.
 */
public final class JackpotSoundState {
	private static boolean enabled;

	private JackpotSoundState() {
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
