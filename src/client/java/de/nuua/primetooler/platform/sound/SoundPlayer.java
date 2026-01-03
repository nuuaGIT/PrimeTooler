package de.nuua.primetooler.platform.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;

/**
 * WARUM: Zentrale API fuer Client-Sounds, ohne Logik in Features zu duplizieren.
 * PERF: Direkter Call auf den LocalPlayer, keine Zwischenschichten.
 */
public final class SoundPlayer {
	private static final float DEFAULT_VOLUME = 1.0f;
	private static final float DEFAULT_PITCH = 1.0f;
	private static float warningVolume = 1.0f;

	private SoundPlayer() {
	}

	public static void play(SoundEvent sound) {
		play(sound, DEFAULT_VOLUME, DEFAULT_PITCH);
	}

	public static void play(SoundEvent sound, float volume, float pitch) {
		if (sound == null) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.player == null) {
			return;
		}
		client.player.playSound(sound, volume, pitch);
	}

	public static void playWarning(SoundEvent sound, float baseVolume, float pitch) {
		float volume = clampVolume(baseVolume * warningVolume);
		if (volume <= 0.0f) {
			return;
		}
		play(sound, volume, pitch);
	}

	public static void setWarningVolume(float volume) {
		warningVolume = clampVolume(volume);
	}

	public static float getWarningVolume() {
		return warningVolume;
	}

	private static float clampVolume(float volume) {
		if (volume < 0.0f) {
			return 0.0f;
		}
		if (volume > 1.0f) {
			return 1.0f;
		}
		return volume;
	}
}
