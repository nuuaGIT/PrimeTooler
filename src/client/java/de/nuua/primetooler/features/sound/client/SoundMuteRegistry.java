package de.nuua.primetooler.features.sound.client;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BooleanSupplier;

/**
 * WHY: Zentrale Registry zum Stummschalten bestimmter Sounds per Feature-Toggle.
 * PERF: Fixer Array-Scan pro Sound-Play, keine Allokationen.
 */
public final class SoundMuteRegistry {
	private static Entry[] entries = new Entry[0];

	private SoundMuteRegistry() {
	}

	public static void register(ResourceLocation id, BooleanSupplier enabled) {
		if (id == null || enabled == null) {
			return;
		}
		Entry[] current = entries;
		for (int i = 0; i < current.length; i++) {
			Entry entry = current[i];
			if (entry.id.equals(id)) {
				entry.enabled = enabled;
				return;
			}
		}
		Entry[] next = new Entry[current.length + 1];
		System.arraycopy(current, 0, next, 0, current.length);
		next[current.length] = new Entry(id, enabled);
		entries = next;
	}

	public static boolean shouldMute(SoundInstance sound) {
		if (sound == null) {
			return false;
		}
		ResourceLocation id = sound.getLocation();
		if (id == null) {
			return false;
		}
		Entry[] current = entries;
		for (int i = 0; i < current.length; i++) {
			Entry entry = current[i];
			if (id.equals(entry.id) && entry.enabled.getAsBoolean()) {
				return true;
			}
		}
		return false;
	}

	private static final class Entry {
		private final ResourceLocation id;
		private BooleanSupplier enabled;

		private Entry(ResourceLocation id, BooleanSupplier enabled) {
			this.id = id;
			this.enabled = enabled;
		}
	}
}
